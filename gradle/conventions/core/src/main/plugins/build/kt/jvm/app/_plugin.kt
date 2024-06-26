package build.kt.jvm.app

import build.api.ProjectPlugin
import build.api.dsl.accessors.application
import build.api.dsl.accessors.distributions
import com.github.jengelman.gradle.plugins.shadow.ShadowApplicationPlugin
import org.gradle.api.distribution.Distribution
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.kotlin.dsl.*

internal const val DIST_APP_HOME_NAME = "appHome"
internal const val DIST_APP_HOME_INSTALL_TASK_NAME = "installAppHomeDist"

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.jvm._plugin>()
		plugin("application")
		plugin("com.github.johnrengelman.shadow")
	}

	val application = application
	val applicationName = provider { application.applicationName }

	val distributions = distributions
	distributions.register(DIST_APP_HOME_NAME) {
		distributionBaseName = applicationName.map { "$it-home" }
	}

	val tasks = tasks
	val installAppHomeDist = tasks.named<Sync>(DIST_APP_HOME_INSTALL_TASK_NAME)

	distributions.named("main") {
		distributionBaseName = applicationName
		setUp(installAppHomeDist)
	}
	distributions.named("shadow") {
		distributionBaseName = applicationName.map { "$it-shadow" }
		setUp(installAppHomeDist)
	}

	val startScripts =
		tasks.named<CreateStartScripts>(ApplicationPlugin.TASK_START_SCRIPTS_NAME) { setUp() }
	tasks.named<CreateStartScripts>(ShadowApplicationPlugin.SHADOW_SCRIPTS_TASK_NAME) { setUp() }

	tasks.named<JavaExec>(ApplicationPlugin.TASK_RUN_NAME) { setUp(installAppHomeDist, startScripts) }
	tasks.named<JavaExec>(ShadowApplicationPlugin.SHADOW_RUN_TASK_NAME) { setUp(installAppHomeDist, startScripts) }
})

private fun Distribution.setUp(installAppHomeDist: TaskProvider<Sync>) {
	contents.from(installAppHomeDist)
}

private fun JavaExec.setUp(installAppHomeDist: TaskProvider<Sync>, startScripts: TaskProvider<CreateStartScripts>) {
	dependsOn(installAppHomeDist)

	val appHomeDir = installAppHomeDist.map { it.destinationDir }
	val appBaseName = startScripts.map { it.applicationName!! }

	doFirst(fun(task) = with(task as JavaExec) {
		appHomeDir.get().let {
			it.mkdirs()
			environment("APP_HOME", it)
		}
		environment("APP_BASE_NAME", appBaseName.get())
	})
}

private fun CreateStartScripts.setUp() {
	doFirst(fun(task) = with(task as CreateStartScripts) {
		// Ensure that there isn't any stale file in the output directory (which
		// may be present if the application name changes).
		outputDir?.deleteRecursively()
	})
}
