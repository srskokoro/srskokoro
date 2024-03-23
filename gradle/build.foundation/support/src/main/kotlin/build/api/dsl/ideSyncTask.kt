package build.api.dsl

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

@Suppress("NOTHING_TO_INLINE")
inline fun Task.runOnIdeSync() = project.runOnIdeSync(this)

@Suppress("NOTHING_TO_INLINE")
inline fun Project.runOnIdeSync(task: Task) = tasks.runOnIdeSync(task)

@Suppress("NOTHING_TO_INLINE")
inline fun Project.runOnIdeSync(task: TaskProvider<*>) = tasks.runOnIdeSync(task)

@Suppress("NOTHING_TO_INLINE")
inline fun TaskContainer.runOnIdeSync(task: Task) {
	ideSyncTask.dependsOn(task)
}

@Suppress("NOTHING_TO_INLINE")
inline fun TaskContainer.runOnIdeSync(task: TaskProvider<*>) {
	ideSyncTask.dependsOn(task)
}

val TaskContainer.ideSyncTask: Task
	// NOTE: Automatically runs on "gradle sync" (via IntelliJ IDEA or Android
	// Studio) -- https://twitter.com/Sellmair/status/1619308362881187840
	get() = maybeCreate("prepareKotlinIdeaImport")
