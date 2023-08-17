package conv.settings

import conv.internal.support.unsafeCast
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState
import org.gradle.kotlin.dsl.typeOf
import java.util.LinkedList

private const val prioritizedEvaluationListeners__name =
	// Named like this to discourage direct access
	"--prioritized-evaluation-listeners"

internal inline var Project.prioritizedEvaluationListeners: LinkedList<Action<in Project>>
	get() = extensions.getByName(prioritizedEvaluationListeners__name).unsafeCast()
	private set(value) {
		extensions.add(typeOf(), prioritizedEvaluationListeners__name, value)
	}

internal object PrioritizedEvaluationListenersSetup : ProjectEvaluationListener {

	override fun beforeEvaluate(project: Project) {
		project.prioritizedEvaluationListeners = LinkedList()
	}

	override fun afterEvaluate(project: Project, state: ProjectState) {
		// NOTE: All actions scheduled via `project.afterEvaluate()` still run
		// even on project evaluation failure due to an exception. We should
		// thus, at least, emulate that behavior.
		var thrown = state.failure
		for (action in project.prioritizedEvaluationListeners) try {
			action.execute(project)
		} catch (ex: Throwable) {
			if (thrown == null) thrown = ex
			else thrown.addSuppressed(ex)
		}
		if (thrown != null)
			throw thrown
	}
}
