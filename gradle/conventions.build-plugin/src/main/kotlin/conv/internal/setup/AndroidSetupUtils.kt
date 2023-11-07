package conv.internal.setup

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType

/**
 * @see AndroidAppExtension
 * @see AndroidLibExtension
 */
internal typealias AndroidExtension = com.android.build.api.dsl.CommonExtension<*, *, *, *, *>

/** @see AndroidExtension */
internal typealias AndroidAppExtension = com.android.build.api.dsl.ApplicationExtension

/** @see AndroidExtension */
internal typealias AndroidLibExtension = com.android.build.api.dsl.LibraryExtension


internal inline fun Project.ifAndroidProject(crossinline block: () -> Unit) {
	plugins.withType<com.android.build.gradle.BasePlugin> {
		block()
	}
}

@JvmName("withAndroid_")
internal fun Project.withAndroid(configure: Action<out AndroidExtension>) {
	ifAndroidProject {
		extensions.configure("android", configure)
	}
}

/**
 * @param T any subtype of [AndroidExtension] (e.g. [AndroidAppExtension] or
 * [AndroidLibExtension]).
 * @see AndroidAppExtension
 * @see AndroidLibExtension
 */
internal fun <T : AndroidExtension> Project.withAndroid(configure: Action<T>) = withAndroid(configure)
