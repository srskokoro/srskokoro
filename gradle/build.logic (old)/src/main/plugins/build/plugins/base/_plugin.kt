package build.plugins.base

import build.api.ProjectPlugin
import build.api.dsl.model.api
import build.api.dsl.model.kotlinSourceSets
import build.api.dsl.model.test
import build.api.dsl.model.testImplementation
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

/**
 * NOTE: The setup of this plugin, along with [build.support.kt.jvm._plugin] and
 * [build.support.kt.base._plugin], should be similar to the `build.gradle.kts`
 * file of the project compiling this plugin. It should be kept consistent with
 * that as much as possible.
 */
class _plugin : ProjectPlugin {

	/**
	 * WARNING: Before making changes to this plugin, please see first the NOTE
	 * provided with this [_plugin] class.
	 */
	override fun Project.applyPlugin() {
		apply {
			plugin<build.support.kt.jvm._plugin>()
			plugin("java-gradle-plugin")
			plugin("org.gradle.kotlin.kotlin-dsl.base")
		}

		kotlinSourceSets.named("main", ::installPluginsAutoRegistrant)

		tasks.test {
			useJUnitPlatform()
			jvmArgs("-ea") // Also enables stacktrace recovery for kotlinx coroutines
		}

		dependencies {
			api("build:build.logic")
			testImplementation(embeddedKotlin("test"))
		}
	}
}
