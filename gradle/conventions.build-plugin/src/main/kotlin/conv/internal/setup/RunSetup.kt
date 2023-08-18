package conv.internal.setup

import isDebug
import org.gradle.api.tasks.JavaExec
import org.gradle.process.JavaForkOptions

internal fun setUp(task: JavaExec): Unit = with(task) {
	if (project.isDebug) setUpForDebug(this)
}

internal fun setUpForDebug(options: JavaForkOptions) = with(options) {
	jvmArgs(mutableListOf<String>().apply(::setUpJvmArgsForDebug))
}

internal fun setUpJvmArgsForDebug(jvmArgs: MutableList<String>) {
	jvmArgs.add("-ea") // Also enables stacktrace recovery for kotlinx coroutines
}
