@file:Suppress("PackageDirectoryMismatch")

import conv.settings.prioritizedEvaluationListeners
import org.gradle.api.Action
import org.gradle.api.Project

fun Project.prioritizedAfterEvaluate(action: Action<in Project>): Boolean {
	(prioritizedEvaluationListeners ?: return false).add(action)
	return true
}
