package srs.kokoro.jcef

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import java.io.File

abstract class JcefExtension(private val project: Project) : ExtensionAware {
	val dependency = jcefMavenDep

	val platform = jcefBuildPlatform
	val recommendedJvmArgs by lazy(LazyThreadSafetyMode.PUBLICATION) {
		if (platform.os.isMacOSX) listOf(
			"--add-opens", "java.desktop/sun.awt=ALL-UNNAMED",
			"--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED",
			"--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
		) else emptyList()
	}

	abstract val outputDir: DirectoryProperty
	fun outputDir(path: Any) = outputDir.set(project.file(path))
	fun outputDir(pathProvider: () -> Any): DirectoryProperty {
		var file: File? = null
		return outputDir.fileProvider(project.provider {
			file ?: project.file(pathProvider()).also { file = it }
		})
	}

	val installTaskName get() = "installJcef"

	private var _installTask: TaskProvider<out JcefInstallTask>? = null
	val installTask
		get() = _installTask ?: project.tasks.register<JcefInstallTask>(
			name = installTaskName, this
		).also { _installTask = it }

	fun installTask(configure: Action<in JcefInstallTask>) = installTask.also { it.configure(configure) }

	@Suppress("NOTHING_TO_INLINE")
	inline fun dependsOnInstallTask(taskName: String) = dependsOnInstallTask(taskName, Task::class.java)

	inline fun <reified T : Task> dependsOnInstallTask(taskName: String, noinline configuration: T.() -> Unit) =
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
