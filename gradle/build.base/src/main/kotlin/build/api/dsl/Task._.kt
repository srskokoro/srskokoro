package build.api.dsl

import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.initialization.IncludedBuild
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider

fun Task.dependOnSameTaskFromSubProjects() {
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
