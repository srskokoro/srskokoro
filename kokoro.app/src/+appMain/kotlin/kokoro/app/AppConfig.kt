package kokoro.app

import kokoro.internal.DEBUG
import kokoro.internal.SPECIAL_USE_DEPRECATION
import kotlinx.atomicfu.update
import kotlinx.atomicfu.updateAndGet
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmField

/**
 * Contains local app preferences; not meant for cloud sync but may be
 * transferable via device-to-device migrations.
 */
@Serializable
data class AppConfig(
	val collectionsPath: String? = null,
) {

	companion object {

		@Suppress("ClassName")
		@Deprecated(SPECIAL_USE_DEPRECATION)
		@PublishedApi
		internal object `-state` {
			@Suppress("DEPRECATION")
			@JvmField val state = if (DEBUG) {
				AppDataImpl.config.asStateFlow()
			} else {
				AppDataImpl.config
			}
		}

		inline val state: StateFlow<AppConfig>
			get() = @Suppress("DEPRECATION") `-state`.state

		@Suppress("NOTHING_TO_INLINE")
		inline fun get(): AppConfig = state.value

		/**
		 * Performs an update atomically using the specified [updateFunction].
		 *
		 * The specified [updateFunction] may run more than once until the
		 * update is successful and atomically performed.
		 *
		 * @see updateAndGet
		 */
		inline fun update(updateFunction: (AppConfig) -> AppConfig) {
			@Suppress("DEPRECATION") AppDataImpl.config.update(updateFunction)
			@Suppress("DEPRECATION") AppDataImpl.scheduleConfigCommit()
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
		inline fun updateAndGet(updateFunction: (AppConfig) -> AppConfig): AppConfig {
			val r = @Suppress("DEPRECATION") AppDataImpl.config.updateAndGet(updateFunction)
			@Suppress("DEPRECATION") AppDataImpl.scheduleConfigCommit()
			return r
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
		inline fun getAndUpdate(updateFunction: (AppConfig) -> AppConfig): AppConfig {
			val r = @Suppress("DEPRECATION") AppDataImpl.config.getAndUpdate(updateFunction)
			@Suppress("DEPRECATION") AppDataImpl.scheduleConfigCommit()
			return r
		}
	}
}
