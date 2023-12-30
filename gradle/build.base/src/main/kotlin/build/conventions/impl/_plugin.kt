package build.conventions.impl

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class _plugin : Plugin<Project> {
	override fun apply(target: Project) {
		target.apply_()
	}
}

private fun Project.apply_() {
	apply {
		plugin("java-gradle-plugin")
		plugin<build.conventions.support.impl._plugin>()
	}

	with((extensions.getByName("kotlin") as ExtensionAware).extensions) {
		configure<NamedDomainObjectContainer<KotlinSourceSet>>("sourceSets") {
			named("main", ::installPluginsAutoRegistrant)
		}
	}
}
