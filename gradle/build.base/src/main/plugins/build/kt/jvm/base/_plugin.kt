package build.kt.jvm.base

import build.api.ProjectPlugin
import build.api.dsl.accessors.test
import build.api.dsl.accessors.testImplementation
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.base._plugin>()
	}

	tasks.test {
		useJUnitPlatform()
		jvmArgs("-ea") // Also enables stacktrace recovery for kotlinx coroutines
	}

	dependencies.run {
		testImplementation(embeddedKotlin("test"))
	}
})
