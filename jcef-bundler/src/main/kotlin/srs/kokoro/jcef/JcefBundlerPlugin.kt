package srs.kokoro.jcef

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class JcefBundlerPlugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.extensions.create<JcefExtension>("jcef", project)
	}
}
