package conv.internal.setup

import isDebug
import org.gradle.jvm.application.tasks.CreateStartScripts

internal fun setUp(task: CreateStartScripts): Unit = with(task) {
	if (project.isDebug) setUpForDebug(this)
}

internal fun setUpForDebug(task: CreateStartScripts) = with(task) {
	defaultJvmOpts = mutableListOf<String>().apply {
		defaultJvmOpts?.let(::addAll)
		setUpJvmArgsForDebug(this)
	}
}
