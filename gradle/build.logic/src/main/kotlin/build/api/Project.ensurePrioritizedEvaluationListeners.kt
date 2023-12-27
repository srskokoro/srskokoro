package build.api

import build.api.dsl.*
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState
import org.gradle.api.invocation.Gradle
import org.gradle.kotlin.dsl.*
import java.util.LinkedList

private const val prioritizedEvaluationListeners__name =
	// Named like this to discourage direct access
	"--prioritizedEvaluationListeners--"

private typealias PrioritizedEvaluationListeners = LinkedList<Action<in Project>>

internal fun Project.ensurePrioritizedEvaluationListeners(): PrioritizedEvaluationListeners {
	return xs().getOrElse(prioritizedEvaluationListeners__name) {
		PrioritizedEvaluationListeners().also { listeners ->
			// Immediately mark as set up, even if the code after it fails.
			add<Any>(prioritizedEvaluationListeners__name, listeners)

			gradle.ensurePrioritizedEvaluationListenersWillExec()
		}
	}
}

private fun Gradle.ensurePrioritizedEvaluationListenersWillExec(): Unit = xs().run {
	if (findByName(prioritizedEvaluationListeners__name) != null) return

	// Immediately mark as set up, even if the code after it fails.
	add(prioritizedEvaluationListeners__name, true)

	gradle().addProjectEvaluationListener(PrioritizedEvaluationListenersSetup)
}

private object PrioritizedEvaluationListenersSetup : ProjectEvaluationListener {

	override fun beforeEvaluate(project: Project) = Unit

	override fun afterEvaluate(project: Project, state: ProjectState) {
		// NOTE: All actions scheduled via `project.afterEvaluate()` still run
		// even on project evaluation failure due to an exception. We should
		// thus, at least, emulate that behavior.
		var thrown = state.failure

		val listeners: PrioritizedEvaluationListeners? =
			project.xs().getSafelyOrNull(prioritizedEvaluationListeners__name)

		if (listeners != null) for (action in listeners) {
			try {
				action.execute(project)
			} catch (ex: Throwable) {
				if (thrown == null) thrown = ex
				else thrown.addSuppressed(ex)
			}
		}

		if (thrown != null)
			throw thrown
	}
}
