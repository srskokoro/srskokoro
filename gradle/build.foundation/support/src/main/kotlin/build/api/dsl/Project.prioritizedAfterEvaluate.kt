package build.api.dsl

import build.api.ensurePrioritizedEvaluationListeners
import org.gradle.api.Action
import org.gradle.api.InvalidUserCodeException
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectStateInternal

fun Project.prioritizedAfterEvaluate(action: Action<in Project>) {
	// Just like `afterEvaluate()`, fail when project is already evaluated.
	val state = state
	if (state is ProjectStateInternal && state.hasCompleted()) throw InvalidUserCodeException(
		"Cannot run `Project.prioritizedAfterEvaluate(Action)` when the project is already evaluated."
	)
	// NOTE: Prioritizes the added action against all previously added actions.
	ensurePrioritizedEvaluationListeners().addFirst(action) // NOT `addLast()`
}
