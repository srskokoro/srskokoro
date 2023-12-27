package build.api.dsl

import build.api.ensurePrioritizedEvaluationListeners
import org.gradle.api.Action
import org.gradle.api.Project

fun Project.prioritizedAfterEvaluate(action: Action<in Project>) {
	// NOTE: Prioritizes the added action against all previously added actions.
	ensurePrioritizedEvaluationListeners().addFirst(action) // NOT `addLast()`
}
