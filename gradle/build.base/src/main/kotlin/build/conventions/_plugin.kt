package build.conventions

import build.conventions.internal.InternalConventions
import build.conventions.internal.contributesPlugins
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class _plugin : Plugin<Project> {
	override fun apply(target: Project) {
		target.apply {
			plugin<build.conventions.support._plugin>()
			plugin("java-gradle-plugin")
		}
		InternalConventions.contributesPlugins(target)
	}
}
