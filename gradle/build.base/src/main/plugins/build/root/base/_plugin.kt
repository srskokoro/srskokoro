package build.root.base

import build.api.ProjectPlugin
import build.api.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin("lifecycle-base")
	}

	afterEvaluate {
		val tasks = tasks

		tasks.named("check") { dependOnTaskFromSubProjects() }
		tasks.named("clean") { dependOnTaskFromSubProjects() }
	}
})
