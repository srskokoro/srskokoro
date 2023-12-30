package build.conventions.root

import build.conventions.internal.throwOnNonConventionsRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class _plugin : Plugin<Project> {
	override fun apply(target: Project) {
		target.throwOnNonConventionsRoot()
		target.apply<build.conventions.root.impl._plugin>()
	}
}
