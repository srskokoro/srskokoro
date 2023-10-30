package kokoro.app

import kotlinx.atomicfu.getAndUpdate
import kotlinx.atomicfu.update
import kotlinx.atomicfu.updateAndGet
import kotlinx.serialization.Serializable

/**
 * Contains local app preferences; not meant for cloud sync but may be
 * transferable via device-to-device migrations.
 */
@Serializable
data class AppConfig(
	val collectionsPath: String? = null,
) {

	companion object {

		@Suppress("NOTHING_TO_INLINE")
		inline fun get(): AppConfig {
			@Suppress("DEPRECATION")
			return AppConfigImpl.holder.value
		}

		/**
		 * Performs an update atomically using the specified [updateFunction].
		 *
		 * The specified [updateFunction] may run more than once until the
		 * update is successful and atomically performed.
		 *
		 * @see updateAndGet
		 */
		inline fun update(crossinline updateFunction: (AppConfig) -> AppConfig) {
			@Suppress("DEPRECATION")
			AppConfigImpl.holder.update { updateFunction(it) }
		}

		/**
		 * Performs an update atomically using the specified [updateFunction]
		 * and returns the new value.
		 *
		 * The specified [updateFunction] may run more than once until the
		 * update is successful and atomically performed.
		 *
		 * @see update
		 */
		inline fun updateAndGet(crossinline updateFunction: (AppConfig) -> AppConfig): AppConfig {
			@Suppress("DEPRECATION")
			return AppConfigImpl.holder.updateAndGet { updateFunction(it) }
		}

		/**
		 * Performs an update atomically using the specified [updateFunction]
		 * and returns the *old value*.
		 *
		 * The specified [updateFunction] may run more than once until the
		 * update is successful and atomically performed.
		 *
		 * @see updateAndGet
		 */
		inline fun getAndUpdate(crossinline updateFunction: (AppConfig) -> AppConfig): AppConfig {
			@Suppress("DEPRECATION")
			return AppConfigImpl.holder.getAndUpdate { updateFunction(it) }
		}
	}
}
