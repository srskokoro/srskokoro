package build.api.dsl

import org.gradle.api.Task
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.UnknownTaskException
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskReference

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

@Suppress("NOTHING_TO_INLINE")
inline fun Task.dependOnTaskFromIncludedBuildsOrFail(vararg includedBuildNames: String) =
	dependOnTaskFromIncludedBuildsOrFail(includedBuildNames.toSet())

fun Task.dependOnTaskFromIncludedBuildsOrFail(includedBuildNames: Set<String>) =
	dependOnTaskFromIncludedBuildsOrFail(project.provider { includedBuildNames })

fun Task.dependOnTaskFromIncludedBuildsOrFail(includedBuildNames: Provider<Set<String>>) {
	dependsOn(includedBuildNames.map { set ->
		val taskName = name
		ArrayList<TaskReference>().also { taskRefs ->
			@Suppress("NAME_SHADOWING") val set = LinkedHashSet(set)
			val gradle = project.gradle
			for (includedBuild in project.gradle.includedBuilds) {
				if (set.remove(includedBuild.name))
					taskRefs.add(includedBuild.task(":$taskName"))
			}
			if (set.isNotEmpty()) {
				throw UnknownDomainObjectException("Included build '${set.first}' not found in $gradle")
			}
		}
	})
}
