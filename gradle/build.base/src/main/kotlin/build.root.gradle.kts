plugins {
	`lifecycle-base`
}

afterEvaluate {
	val tasks = tasks

	tasks.named("check").configure { dependOnTaskFromSubProjects() }
	tasks.maybeRegister("test").configure { dependOnTaskFromSubProjects() }

	tasks.named("clean").configure { dependOnTaskFromSubProjects() }
}
