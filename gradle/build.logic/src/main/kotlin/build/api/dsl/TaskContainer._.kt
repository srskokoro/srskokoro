package build.api.dsl

import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.gradle.language.base.plugins.LifecycleBasePlugin

inline fun <reified T : Task> TaskContainer.namedOrNull(name: String) = namedOrNull(name, T::class.java)

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Task> TaskContainer.namedOrNull(name: String, type: Class<T>): TaskProvider<T>? {
	return try {
		// NOTE: Rather than check via `getNames()`, it's better to just let the
		// following throw (and catch the exception), since `getNames()` seems
		// to be not optimized for repeated access (as it may return a different
		// instance every time).
		@Suppress("UNCHECKED_CAST")
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

// --

@Suppress("NOTHING_TO_INLINE")
inline fun TaskContainer.maybeRegister(name: String) = maybeRegister<Task>(name)

@JvmName("maybeRegister reified") @JvmSynthetic
inline fun <reified T : Task> TaskContainer.maybeRegister(name: String) = maybeRegister(name, T::class.java)

fun <T : Task> TaskContainer.maybeRegister(name: String, type: Class<T>) =
	namedOrElse(name, type) { register(name, type) }


@Suppress("NOTHING_TO_INLINE")
inline fun TaskContainer.maybeRegister(name: String, configuration: Action<in Task>) =
	maybeRegister<Task>(name, configuration)

@JvmName("maybeRegister reified") @JvmSynthetic
inline fun <reified T : Task> TaskContainer.maybeRegister(name: String, configuration: Action<in T>) =
	maybeRegister(name, T::class.java, configuration)

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Task> TaskContainer.maybeRegister(name: String, type: Class<T>, configuration: Action<in T>) =
	maybeRegister(name, type).apply { configure(configuration) }

// --

fun TaskContainer.registerTestTask() = register<Test>("test") {
	group = LifecycleBasePlugin.VERIFICATION_GROUP
	testClassesDirs = project.files()
}

@Suppress("NOTHING_TO_INLINE")
inline fun TaskContainer.registerTestTask(configuration: Action<in Test>) =
	registerTestTask().apply { configure(configuration) }


fun TaskContainer.maybeRegisterTestTask(): TaskProvider<Task> =
	namedOrElse<Task>("test") { registerTestTask() }

@Suppress("NOTHING_TO_INLINE")
inline fun TaskContainer.maybeRegisterTestTask(configuration: Action<in Task>) =
	maybeRegisterTestTask().apply { configure(configuration) }
