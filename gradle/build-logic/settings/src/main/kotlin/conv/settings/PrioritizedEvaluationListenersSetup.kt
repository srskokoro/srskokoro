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

internal inline val Project.prioritizedEvaluationListeners
	// NOTE: Given that the extension below is set via the `settings` script, it
	// will be null if Gradle simply evaluated a fake project in order to
	// generate type-safe model accessors for precompiled script plugins to use.
	get() = extensions.findByName(prioritizedEvaluationListeners__name)
		// NOTE: The cast below throws on non-null incompatible types (as intended).
		.unsafeCast<LinkedList<Action<in Project>>?>()

private inline var Project.prioritizedEvaluationListeners_: LinkedList<Action<in Project>>
	get() = extensions.getByName(prioritizedEvaluationListeners__name).unsafeCast()
	private set(value) {
		extensions.add(typeOf(), prioritizedEvaluationListeners__name, value)
	}

internal object PrioritizedEvaluationListenersSetup : ProjectEvaluationListener {

	override fun beforeEvaluate(project: Project) {
		project.prioritizedEvaluationListeners_ = LinkedList()
	}

	override fun afterEvaluate(project: Project, state: ProjectState) {
		// NOTE: All actions scheduled via `project.afterEvaluate()` still run
		// even on project evaluation failure due to an exception. We should
		// thus, at least, emulate that behavior.
		var thrown = state.failure
		for (action in project.prioritizedEvaluationListeners_) try {
			action.execute(project)
		} catch (ex: Throwable) {
			if (thrown == null) thrown = ex
			else thrown.addSuppressed(ex)
		}
		if (thrown != null)
			throw thrown
	}
}
