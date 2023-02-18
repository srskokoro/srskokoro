package convention.internal

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType

internal inline fun Project.ifAndroidProject(crossinline block: () -> Unit) {
	plugins.withType<com.android.build.gradle.BasePlugin> {
		block()
	}
}

internal fun Project.withAndroid(configure: Action<com.android.build.api.dsl.CommonExtension<*, *, *, *>>) {
	ifAndroidProject {
		extensions.configure("android", configure)
	}
}
