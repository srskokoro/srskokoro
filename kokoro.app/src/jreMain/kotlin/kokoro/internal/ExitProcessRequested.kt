package kokoro.internal

/**
 * @see ExitProcessRequested.discard
 * @see ExitProcessRequested.discardIn
 * @see ExitProcessRequested.Catcher
 */
class ExitProcessRequested : Throwable(null, null, true, false) {

	companion object {

		/** @see ExitProcessRequested.Catcher */
		fun installCatcherThenThrow(): ExitProcessRequested {
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

	/**
	 * @see install
	 * @see uninstall
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
