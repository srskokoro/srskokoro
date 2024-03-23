package build.api.dsl.accessors

import build.api.dsl.*
import org.gradle.api.Project
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

val Project.gradlePlugin: GradlePluginDevelopmentExtension
	get() = x("gradlePlugin")


val Project.base: BasePluginExtension
	get() = x("base")

val Project.baseOrNull: BasePluginExtension?
	get() = xs().getSafelyOrNull("base")

val Project.distributions: DistributionContainer
	get() = x("distributions")

val Project.application: JavaApplication
	get() = x("application")


val Project.sourceSets: SourceSetContainer
	get() = x("sourceSets")
