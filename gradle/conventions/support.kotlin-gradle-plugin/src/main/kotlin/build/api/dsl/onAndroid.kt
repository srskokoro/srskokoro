package build.api.dsl

import org.gradle.api.Project

/**
 * @param T any subtype of [AndroidExtension] (e.g. [AndroidAppExtension] or
 * [AndroidLibExtension]).
 *
 * @see AndroidAppExtension
 * @see AndroidLibExtension
 */
inline fun <T : AndroidExtension> Project.onAndroid(crossinline configure: T.() -> Unit) {
	onAndroidProject {
		@Suppress("UNCHECKED_CAST")
		configure(projectThis.extensions as T)
	}
}

/**
 * @see onAndroid
 */
@JvmName("withAndroid_")
inline fun Project.onAndroid(crossinline configure: AndroidExtension.() -> Unit) =
	onAndroid<AndroidExtension>(configure)
