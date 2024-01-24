package build.api.dsl

import org.gradle.api.Action
import org.gradle.api.Project

@JvmName("withAndroid_")
fun Project.withAndroid(configure: Action<out AndroidExtension>) {
	ifAndroidProject {
		projectThis.extensions.configure("android", configure)
	}
}

/**
 * @param T any subtype of [AndroidExtension] (e.g. [AndroidAppExtension] or
 * [AndroidLibExtension]).
 *
 * @see AndroidAppExtension
 * @see AndroidLibExtension
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T : AndroidExtension> Project.withAndroid(configure: Action<T>) = withAndroid(configure)
