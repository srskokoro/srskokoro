package build.api.dsl.accessors

import build.api.dsl.*
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension

val Project.base: BasePluginExtension
	get() = x("base")

val Project.baseOrNull: BasePluginExtension?
	get() = xs().getSafelyOrNull("base")
