package srs.kokoro.jcef

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.register
import java.util.*
import javax.inject.Inject

abstract class JcefExtension @Inject constructor(
	private val project: Project,
) : ExtensionAware {

	internal companion object {
		const val DEFAULT_TASK_GROUP = "jcef"
		const val DEFAULT_INSTALL_TASK_NAME = "installJcef"
	}

	val platform get() = jcefBuildPlatform

	val dependency = jcefMavenDep
	val recommendedJvmArgs by lazy(LazyThreadSafetyMode.PUBLICATION) {
		if (platform.os.isMacOSX) listOf(
			"--add-opens", "java.desktop/sun.awt=ALL-UNNAMED",
			"--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED",
			"--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
		) else emptyList()
	}

	val installTask = project.tasks.register<JcefInstallTask>(DEFAULT_INSTALL_TASK_NAME)

	fun installTask(configure: Action<in JcefInstallTask>) = installTask.also { it.configure(configure) }

	@Suppress("NOTHING_TO_INLINE")
	inline fun dependsOnInstallTask(taskName: String) = dependsOnInstallTask(taskName, Task::class.java)

	inline fun <reified T : Task> dependsOnInstallTask(taskName: String, noinline configuration: T.() -> Unit = {}) =
		dependsOnInstallTask(taskName, T::class.java, configuration)

	fun <T : Task> dependsOnInstallTask(taskName: String, type: Class<T>, configuration: T.() -> Unit = {}) {
		val installTask = installTask
		project.afterEvaluate {
			tasks.named(taskName, type) {
				dependsOn(installTask)
				configuration()
			}
		}
	}
}
