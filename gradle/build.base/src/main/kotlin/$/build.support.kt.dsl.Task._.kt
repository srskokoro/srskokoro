@file:Suppress("PackageDirectoryMismatch")

import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.provider.Provider

fun Task.dependOnTaskFromSubProjects() {
	val taskName = name
	val project = project
	dependsOn(project.provider {
		project.subprojects.mapNotNull { project ->
			try {
				project.tasks.named(taskName)
			} catch (_: UnknownTaskException) {
				null
			}
		}
	})
}

fun Task.dependOnTaskFromIncludedBuildsOrFail() {
	val taskName = name
	dependsOn(project.gradle.includedBuilds.map { it.task(":$taskName") })
}

fun Task.dependOnTaskFromIncludedBuildsOrFail(vararg includedBuildNames: String) =
	dependOnTaskFromIncludedBuildsOrFail(project.provider { includedBuildNames.toSet() })

fun Task.dependOnTaskFromIncludedBuildsOrFail(includedBuildNames: Set<String>) =
	dependOnTaskFromIncludedBuildsOrFail(project.provider { includedBuildNames })

fun Task.dependOnTaskFromIncludedBuildsOrFail(includedBuildNames: Provider<Set<String>>) {
	dependsOn(includedBuildNames.map { set ->
		val taskName = name
		project.gradle.includedBuilds.mapNotNull {
			if (it.name in set) it.task(":$taskName") else null
		}
	})
}
