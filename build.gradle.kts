plugins {
	id("build.root")
}

tasks {
	fun Task.dependOnTasksFromIncludedBuilds() {
		val name = name
		dependsOn(project.gradle.includedBuilds.map { it.task(":$name") })
	}
	check.configure { dependOnTasksFromIncludedBuilds() }
}
