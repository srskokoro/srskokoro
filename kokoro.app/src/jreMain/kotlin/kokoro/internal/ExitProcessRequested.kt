package kokoro.internal

import kotlin.coroutines.cancellation.CancellationException

/**
 * @see ExitProcessRequested.discard
 * @see ExitProcessRequested.discardIn
 * @see ExitProcessRequested.Catcher
 */
class ExitProcessRequested : CancellationException(null) {

	override fun fillInStackTrace(): Throwable = this

	init {
		initCause(null)
	}

	val safeMessage: String? get() = super.message

	override val message: String?
		get() {
			// If we're here, then assume that this throwable was caught and
			// intercepted for printing or logging.
			if (CleanProcessExit.isExiting) {
				// Assume that after this throwable is printed or logged, it'll
				// be discarded in a way that it won't reach our `Catcher`, so
				// uninstall the `Catcher` now (if any was installed).
				Catcher.uninstall()
			}
			return super.message
		}

	companion object {

		/** @see ExitProcessRequested.Catcher */
		@Suppress("NOTHING_TO_INLINE")
		@JvmName("installCatcherThenThrow_")
		inline fun installCatcherThenThrow(): Nothing {
			@Suppress("DEPRECATION_ERROR")
			throw installCatcherThenThrow_()
		}

		@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
		@JvmName("installCatcherThenThrow")
		fun installCatcherThenThrow_(): ExitProcessRequested {
			Catcher.install()
			throw ExitProcessRequested()
		}
	}

	/** @see discardIn */
	@Suppress("NOTHING_TO_INLINE")
	inline fun discard() = discardIn(Thread.currentThread())

	/** @see discard */
	fun discardIn(t: Thread) {
		Catcher.uninstallIn(t)
		throwAnySuppressed()
	}

	fun throwAnySuppressed() {
		val suppressed = suppressed
		if (suppressed.isEmpty()) return

		val ex = suppressed[0]
		for (i in 1..<suppressed.size) {
			@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
			(ex as java.lang.Throwable).addSuppressed(suppressed[i])
		}
		if (ex !is ExitProcessRequested) throw ex
		ex.throwAnySuppressed()
	}

	/**
	 * @see Catcher.install
	 * @see Catcher.uninstall
	 */
	class Catcher(val base: Thread.UncaughtExceptionHandler) : Thread.UncaughtExceptionHandler {

		override fun uncaughtException(t: Thread?, ex: Throwable?) {
			@Suppress("NAME_SHADOWING") var ex = ex
			if (ex is ExitProcessRequested) try {
				ex.throwAnySuppressed()
				return // Skip code below
			} catch (exx: Throwable) {
				ex = exx
			}
			base.uncaughtException(t, ex)
		}

		companion object {

			/** @see installIn */
			@Suppress("NOTHING_TO_INLINE")
			inline fun install() = installIn(Thread.currentThread())

			/** @see install */
			fun installIn(t: Thread) {
				val b = t.uncaughtExceptionHandler ?: return // Dead thread
				if (b !is Catcher)
					t.uncaughtExceptionHandler = Catcher(b)
			}

			/** @see uninstallIn */
			@Suppress("NOTHING_TO_INLINE")
			inline fun uninstall() = uninstallIn(Thread.currentThread())

			/** @see uninstall */
			fun uninstallIn(t: Thread) {
				val x: Thread.UncaughtExceptionHandler? =
					t.uncaughtExceptionHandler // `null` if thread already dead

				if (x is Catcher) {
					val b = x.base
					// NOTE: The thread can be terminated asynchronously here
					// but we don't care.
					t.uncaughtExceptionHandler = if (b === t.threadGroup) null else b
				}
			}
		}
	}
}
