package build.root

import build.api.ProjectPlugin
import build.api.dsl.*
import org.gradle.api.Project

class _plugin : ProjectPlugin {

	override fun Project.applyPlugin() {
		apply {
			plugin("lifecycle-base")
		}

		afterEvaluate {
			val tasks = tasks

			tasks.named("check") { dependOnTaskFromSubProjects() }
			tasks.maybeRegisterTestTask { dependOnTaskFromSubProjects() }

			tasks.named("clean") { dependOnTaskFromSubProjects() }
		}
	}
}
