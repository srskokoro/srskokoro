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
		state.rethrowFailure()
		for (action in project.prioritizedEvaluationListeners)
			action.execute(project)
	}
}
