package build.conventions.support

import build.conventions.internal.throwOnNonConventionsRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class _plugin : Plugin<Project> {
	override fun apply(target: Project) {
		target.throwOnNonConventionsRoot()
		target.apply<build.conventions.support.impl._plugin>()
	}
}
