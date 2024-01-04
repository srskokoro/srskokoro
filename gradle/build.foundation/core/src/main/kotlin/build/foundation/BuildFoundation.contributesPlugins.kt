package build.foundation

import build.conventions.installPluginsAutoRegistrant
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

/**
 * WARNING: Assumes that [java-gradle-plugin][JavaGradlePluginPlugin] plugin has
 * already been applied.
 */
fun BuildFoundation.contributesPlugins(project: Project) {
	with((project.extensions.getByName("kotlin") as ExtensionAware).extensions) {
		configure<NamedDomainObjectContainer<KotlinSourceSet>>("sourceSets") {
			named("main", ::installPluginsAutoRegistrant)
		}
	}
}
