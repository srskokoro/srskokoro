package kokoro.app

import java.util.concurrent.atomic.AtomicReference

data class AppDataOverrides(
	@JvmField val defaultRoot: String? = null,

	@JvmField val roamingRoot: String? = null,
	@JvmField val localRoot: String? = null,
	@JvmField val cacheRoot: String? = null,
) {
	private var isPlaceholder = false

	companion object {
		private val ref = AtomicReference(AppDataOverrides().apply { isPlaceholder = true })

		fun get() = ref.get()
	}

	fun install() {
		val ref = ref
		do {
			val prev = ref.get()
			if (!prev.isPlaceholder) {
				if (this == prev) break
				throw InstallConflictException()
			}
		} while (!ref.weakCompareAndSetVolatile(prev, this))
	}

	@Suppress("unused")
	class InstallConflictException : IllegalStateException {
		constructor()
		constructor(message: String?) : super(message)
		constructor(message: String?, cause: Throwable?) : super(message, cause)
		constructor(cause: Throwable?) : super(cause)
	}
}
