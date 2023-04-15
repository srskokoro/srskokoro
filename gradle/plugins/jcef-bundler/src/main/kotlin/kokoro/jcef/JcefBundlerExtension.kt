package kokoro.jcef

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.register
import javax.inject.Inject

abstract class JcefBundlerExtension @Inject internal constructor(
	private val project: Project,
) : JcefDependencyExtension() {

	internal companion object {
		const val DEFAULT_INSTALL_TASK_NAME = "installJcef"
	}

	val installTask = project.tasks.register<JcefInstallTask>(DEFAULT_INSTALL_TASK_NAME) {
		group = DEFAULT_TASK_GROUP
		description = "Installs native binaries provided by JCEF Maven."
	}

	fun installTask(configure: Action<in JcefInstallTask>) = installTask.also { it.configure(configure) }

	@Suppress("NOTHING_TO_INLINE")
	inline fun dependsOnInstallTask(taskName: String) = dependsOnInstallTask(taskName, Task::class.java)

	inline fun <reified T : Task> dependsOnInstallTask(
		taskName: String, noinline configuration: T.(JcefInstallTask) -> Unit = {}
	) = dependsOnInstallTask(taskName, T::class.java, configuration)

	fun <T : Task> dependsOnInstallTask(
		taskName: String, type: Class<T>, configuration: T.(JcefInstallTask) -> Unit = {}
	) {
		val installTask = installTask
		project.afterEvaluate {
			tasks.named(taskName, type) {
				installTask.get().let {
					dependsOn(it)
					configuration(it)
				}
			}
		}
	}
}
