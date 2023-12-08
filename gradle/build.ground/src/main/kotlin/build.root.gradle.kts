plugins {
	`lifecycle-base`
}

afterEvaluate {
	fun Task.dependOnTasksFromSubProjects() {
		val project = project
		val name = name
		dependsOn(project.provider {
			project.subprojects.mapNotNull { project ->
				try {
					project.tasks.named(name)
				} catch (_: UnknownTaskException) {
					null
				}
			}
		})
	}

	fun TaskContainer.maybeRegister(name: String) = try {
		// NOTE: Rather than check via `getNames()`, it's better to just let the
		// following throw (and catch the exception), since `getNames()` seems
		// to be not optimized for repeated access (as it may return a different
		// instance every time).
		named(name)
	} catch (_: UnknownTaskException) {
		register(name)
	}

	val tasks = tasks
	tasks.maybeRegister("check").configure { dependOnTasksFromSubProjects() }
	tasks.maybeRegister("test").configure { dependOnTasksFromSubProjects() }
}
