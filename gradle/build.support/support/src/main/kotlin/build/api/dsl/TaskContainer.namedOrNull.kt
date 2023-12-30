package build.api.dsl

import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

inline fun <reified T : Task> TaskContainer.namedOrNull(name: String) = namedOrNull(name, T::class.java)

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Task> TaskContainer.namedOrNull(name: String, type: Class<T>): TaskProvider<T>? {
	return try {
		// NOTE: Rather than check via `getNames()`, it's better to just let the
		// following throw (and catch the exception), since `getNames()` seems
		// to be not optimized for repeated access (as it may return a different
		// instance every time).
		named(name, type)
	} catch (_: UnknownTaskException) {
		null
	}
}

inline fun <reified T : Task> TaskContainer.namedOrElse(name: String, defaultValue: TaskContainer.(name: String) -> TaskProvider<out T>) =
	namedOrElse(name, T::class.java, defaultValue)

inline fun <T : Task> TaskContainer.namedOrElse(
	name: String, type: Class<T>,
	defaultValue: TaskContainer.(name: String) -> TaskProvider<out T>,
): TaskProvider<T> {
	return try {
		// NOTE: Rather than check via `getNames()`, it's better to just let the
		// following throw (and catch the exception), since `getNames()` seems
		// to be not optimized for repeated access (as it may return a different
		// instance every time).
		named(name, type)
	} catch (_: UnknownTaskException) {
		@Suppress("UNCHECKED_CAST")
		defaultValue(name) as TaskProvider<T>
	}
}
