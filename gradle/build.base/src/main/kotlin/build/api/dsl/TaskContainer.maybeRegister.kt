package build.api.dsl

import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer

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
