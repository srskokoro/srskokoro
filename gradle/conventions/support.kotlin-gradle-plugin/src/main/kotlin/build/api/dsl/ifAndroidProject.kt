package build.api.dsl

import org.gradle.api.Project

/**
 * @see com.android.build.gradle.api.AndroidBasePlugin
 */
inline fun Project.ifAndroidProject(crossinline block: () -> Unit) {
	plugins.withId("com.android.base") {
		block()
	}
}
