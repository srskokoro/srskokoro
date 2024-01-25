package build.api.dsl

import build.api.dsl.accessors.android
import org.gradle.api.Action
import org.gradle.api.Project

/**
 * @param T any subtype of [AndroidExtension] (e.g. [AndroidAppExtension] or
 * [AndroidLibExtension]).
 *
 * @see AndroidAppExtension
 * @see AndroidLibExtension
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T : AndroidExtension> Project.onAndroid(configure: Action<T>) = onAndroid(configure)

/**
 * @see onAndroid
 */
@JvmName("withAndroid_")
fun Project.onAndroid(configure: Action<out AndroidExtension>) {
	pluginManager.withPlugin("com.android.base") {
		// NOTE: At this point, AGP isn't fully applied yet.
		val android = android
		when (android) {
			is AndroidAppExtension -> "com.android.application"
			is AndroidLibExtension -> "com.android.library"
			is AndroidDynamicFeatureExtension -> "com.android.dynamic-feature"
			is AndroidTestExtension -> "com.android.test"
			else -> error("Unknown `android` extension $android")
		}.let { pluginId ->
			pluginManager.withPlugin(pluginId) {
				@Suppress("UNCHECKED_CAST")
				(configure as Action<AndroidExtension>).execute(android)
			}
		}
	}
}
