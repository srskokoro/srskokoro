package conv.internal.util

import conv.internal.support.unsafeCast
import conv.internal.support.unsafeCastOrNull
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.ProjectEvaluationListener
import org.gradle.api.ProjectState
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.add
import java.util.LinkedList

private const val prioritizedEvaluationListeners__name =
	// Named like this to discourage direct access
	"--prioritized-evaluation-listeners"

private typealias PrioritizedEvaluationListeners = LinkedList<Action<in Project>>

internal fun Project.ensurePrioritizedEvaluationListeners(): PrioritizedEvaluationListeners {
	extensions.findByName(prioritizedEvaluationListeners__name)
		// NOTE: The cast below throws on non-null incompatible types (as intended).
		.unsafeCast<PrioritizedEvaluationListeners?>()
		?.let { return it }

	val listeners = PrioritizedEvaluationListeners()

	// Immediately mark as set up, even if the code after it fails.
	extensions.add<Any>(prioritizedEvaluationListeners__name, listeners)

	val gradle = gradle
	val xs = (gradle as ExtensionAware).extensions
	if (xs.findByName(prioritizedEvaluationListeners__name) == null) {
		xs.add(prioritizedEvaluationListeners__name, true) // Immediately mark as set up, even if the code after it fails.
		gradle.addProjectEvaluationListener(PrioritizedEvaluationListenersSetup)
	}

	return listeners
}

private object PrioritizedEvaluationListenersSetup : ProjectEvaluationListener {

	override fun beforeEvaluate(project: Project) = Unit

	override fun afterEvaluate(project: Project, state: ProjectState) {
		// NOTE: All actions scheduled via `project.afterEvaluate()` still run
		// even on project evaluation failure due to an exception. We should
		// thus, at least, emulate that behavior.
		var thrown = state.failure

		val listeners = project.extensions
			.findByName(prioritizedEvaluationListeners__name)
			.unsafeCastOrNull<PrioritizedEvaluationListeners?>()

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
