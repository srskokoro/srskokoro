package build.api.dsl

import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskContainer

fun TaskContainer.maybeRegister(name: String) = try {
	// NOTE: Rather than check via `getNames()`, it's better to just let the
	// following throw (and catch the exception), since `getNames()` seems to be
	// not optimized for repeated access (as it may return a different instance
	// every time).
	named(name)
} catch (_: UnknownTaskException) {
	register(name)
}
