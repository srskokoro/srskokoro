package build.conventions.root.impl

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension
import java.io.File

class _plugin : Plugin<Project> {
	override fun apply(target: Project) {
		target.apply_()

		target.plugins.withType<YarnPlugin>(fun(_) {
			with(target.extensions.getByName(YarnRootExtension.YARN) as YarnRootExtension) {
				lockFileDirectory = File(target.projectDir, "#kotlin-js-store")
			}
		})
	}
}

private fun Project.apply_() {
	check(parent == null) { "Must only be applied to the root project" }

	allprojects {
		layout.buildDirectory.set(file(".build"))
	}

	apply {
		plugin("lifecycle-base")
	}

	afterEvaluate {
		tasks.run {
			named("check") { dependOnSameTaskFromSubProjects() }
			named("clean") { dependOnSameTaskFromSubProjects() }
		}
	}
}

private fun Task.dependOnSameTaskFromSubProjects() {
	val taskName = name
	val project = project
	dependsOn(project.provider(fun() = ArrayList<TaskProvider<Task>>().apply {
		project.subprojects.forEach { project ->
			try {
				// NOTE: Rather than check via `getNames()`, it's better to just
				// let the following throw (and catch the exception), since
				// `getNames()` seems to be not optimized for repeated access
				// (as it may return a different instance every time).
				add(project.tasks.named(taskName))
			} catch (_: UnknownTaskException) {
				// Ignore
			}
		}
	}))
}
