package srs.kokoro.jcef

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register
import java.util.*
import javax.inject.Inject

abstract class JcefExtension private constructor(
	private val project: Project,
	internal val config: JcefConfig,
) : JcefConfig by config, ExtensionAware {
	@Suppress("unused")
	@Inject
	constructor(project: Project) : this(project, project.objects.newInstance(JcefConfigImpl::class))

	val dependency = jcefMavenDep
	val recommendedJvmArgs by lazy(LazyThreadSafetyMode.PUBLICATION) {
		if (platform.os.isMacOSX) listOf(
			"--add-opens", "java.desktop/sun.awt=ALL-UNNAMED",
			"--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED",
			"--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
		) else emptyList()
	}

	private var _installTask: TaskProvider<out JcefInstallTask>? = null
	val installTask
		get() = _installTask ?: project.tasks.register<JcefInstallTask>(
			name = installTaskName, this
		).also { _installTask = it }

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
