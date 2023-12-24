package build.api.dsl

import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.initialization.IncludedBuild
import org.gradle.api.provider.Provider

fun Task.dependOnSameTaskFromSubProjects() {
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

fun Task.dependOnSameTaskFromIncludedBuildsOrFail() =
	dependOnSameTaskFromIncludedBuildsOrFail(project.gradle.includedBuilds)

fun Task.dependOnSameTaskFromIncludedBuildsOrFail(includedBuilds: Iterable<IncludedBuild>) {
	val taskPath = ":$name"
	dependsOn(includedBuilds.map { it.task(taskPath) })
}

fun Task.dependOnSameTaskFromIncludedBuildsOrFail(includedBuilds: Provider<out Iterable<IncludedBuild>>) {
	val taskPath = ":$name"
	dependsOn(includedBuilds.map { iter -> iter.map { it.task(taskPath) } })
}
