package build.api.dsl

import org.gradle.api.Project

/**
 * @see com.android.build.gradle.api.AndroidBasePlugin
 */
inline fun Project.onAndroidProject(crossinline block: () -> Unit) {
	pluginManager.withPlugin("com.android.base") {
		block()
	}
}
