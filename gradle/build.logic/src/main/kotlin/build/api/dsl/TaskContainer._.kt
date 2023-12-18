package build.api.dsl

import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

@Suppress("NOTHING_TO_INLINE")
inline fun TaskContainer.maybeRegister(name: String) = maybeRegister<Task>(name)

@JvmName("maybeRegister reified") @JvmSynthetic
inline fun <reified T : Task> TaskContainer.maybeRegister(name: String) = maybeRegister(name, T::class.java)

fun <T : Task> TaskContainer.maybeRegister(name: String, type: Class<T>): TaskProvider<T> = try {
	// NOTE: Rather than check via `getNames()`, it's better to just let the
	// following throw (and catch the exception), since `getNames()` seems to be
	// not optimized for repeated access (as it may return a different instance
	// every time).
	named(name, type)
} catch (_: UnknownTaskException) {
	register(name, type)
}


@Suppress("NOTHING_TO_INLINE")
inline fun TaskContainer.maybeRegister(name: String, configuration: Action<in Task>) =
	maybeRegister<Task>(name, configuration)

@JvmName("maybeRegister reified") @JvmSynthetic
inline fun <reified T : Task> TaskContainer.maybeRegister(name: String, configuration: Action<in T>) =
	maybeRegister(name, T::class.java, configuration)

fun <T : Task> TaskContainer.maybeRegister(name: String, type: Class<T>, configuration: Action<in T>) =
	maybeRegister(name, type).configure(configuration)
