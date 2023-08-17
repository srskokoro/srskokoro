@file:Suppress("PackageDirectoryMismatch")

import conv.settings.prioritizedEvaluationListeners
import org.gradle.api.Action
import org.gradle.api.Project

fun Project.prioritizedAfterEvaluate(action: Action<in Project>) {
	prioritizedEvaluationListeners.add(action)
}
