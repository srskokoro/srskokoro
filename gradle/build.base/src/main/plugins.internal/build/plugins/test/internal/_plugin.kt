package build.plugins.test.internal

import build.api.ProjectPlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import java.io.File

class _plugin : ProjectPlugin({
	tasks.withType<Test>().configureEach {
		doFirst {
			systemProperty("build.plugins.test.classpath", classpath.joinToString(File.pathSeparator))
		}
	}
})
