package build.api.dsl

import org.gradle.api.Task

fun Task.recurseTaskTree() = mutableSetOf<Task>().also { recurseTaskTreeTo(it) }

fun Task.recurseTaskTreeTo(output: MutableSet<Task>) {
	if (output.add(this)) recurseTaskDependenciesTo(output)
}

fun Task.recurseTaskDependencies() = mutableSetOf<Task>().also { recurseTaskDependenciesTo(it) }

fun Task.recurseTaskDependenciesTo(output: MutableSet<Task>) {
	for (d in taskDependencies.getDependencies(this)) {
		if (output.add(d)) d.recurseTaskDependenciesTo(output)
	}
}
