package build.plugins.base.internal

import build.api.ProjectPlugin
import build.api.dsl.*
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import java.io.File

class _plugin : ProjectPlugin({
	apply {
		plugin<build.base.internal._plugin>()
		plugin<build.kt.base.internal._plugin>()
		plugin<build.support.kt.internal._plugin>()
	}

	tasks.withType<Test>().configureEach {
		doFirst {
			systemProperty("build.plugins.test.classpath", classpath.joinToString(File.pathSeparator))
		}
	}

	configurations.configureEach {
		if (!isCanBeResolved) return@configureEach // Skip

		// Fail on transitive upgrade/downgrade of direct dependency versions
		failOnDirectDependencyVersionGotcha(this, excludeFilter = fun(it): Boolean {
			// Exclude dependencies added automatically by plugins (whether that
			// be our plugins or the built-in ones provided by Gradle).
			run<Unit> {
				if (it.group == "org.jetbrains.kotlin") when (it.name) {
					"kotlin-gradle-plugin" -> return@run
					"kotlin-reflect" -> return@run
					"kotlin-stdlib" -> return@run
					"kotlin-test" -> return@run
					else -> return false // Don't exclude
				}
			}
			return true // Do exclude
		})
	}
})
