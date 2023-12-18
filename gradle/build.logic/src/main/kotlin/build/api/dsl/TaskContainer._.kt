package build.api.dsl

import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.gradle.language.base.plugins.LifecycleBasePlugin

@Suppress("NOTHING_TO_INLINE")
inline fun TaskContainer.maybeRegister(name: String) = maybeRegister<Task>(name)

@JvmName("maybeRegister reified") @JvmSynthetic
inline fun <reified T : Task> TaskContainer.maybeRegister(name: String) = maybeRegister(name, T::class.java)

fun <T : Task> TaskContainer.maybeRegister(name: String, type: Class<T>): TaskProvider<T> {
	return try {
		// NOTE: Rather than check via `getNames()`, it's better to just let the
		// following throw (and catch the exception), since `getNames()` seems
		// to be not optimized for repeated access (as it may return a different
		// instance every time).
		named(name, type)
	} catch (_: UnknownTaskException) {
		register(name, type)
	}
}


@Suppress("NOTHING_TO_INLINE")
inline fun TaskContainer.maybeRegister(name: String, configuration: Action<in Task>) =
	maybeRegister<Task>(name, configuration)

@JvmName("maybeRegister reified") @JvmSynthetic
inline fun <reified T : Task> TaskContainer.maybeRegister(name: String, configuration: Action<in T>) =
	maybeRegister(name, T::class.java, configuration)

fun <T : Task> TaskContainer.maybeRegister(name: String, type: Class<T>, configuration: Action<in T>): TaskProvider<T> {
	return try {
		// NOTE: Rather than check via `getNames()`, it's better to just let the
		// following throw (and catch the exception), since `getNames()` seems
		// to be not optimized for repeated access (as it may return a different
		// instance every time).
		named(name, type, configuration)
	} catch (_: UnknownTaskException) {
		register(name, type, configuration)
	}
}


fun TaskContainer.maybeRegisterTestLifecycleTask(): TaskProvider<out Task> {
	return try {
		// NOTE: Rather than check via `getNames()`, it's better to just let the
		// following throw (and catch the exception), since `getNames()` seems
		// to be not optimized for repeated access (as it may return a different
		// instance every time).
		named("test")
	} catch (_: UnknownTaskException) {
		register<Test>("test") {
			group = LifecycleBasePlugin.VERIFICATION_GROUP
			testClassesDirs = project.files()
		}
	}
}

@Suppress("NOTHING_TO_INLINE")
inline fun TaskContainer.maybeRegisterTestLifecycleTask(configuration: Action<in Task>) =
	maybeRegisterTestLifecycleTask().apply { configure(configuration) }
