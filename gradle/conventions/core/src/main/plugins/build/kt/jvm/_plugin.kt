package build.kt.jvm

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.application
import build.api.dsl.accessors.testImplementation
import build.setUp
import com.github.jengelman.gradle.plugins.shadow.ShadowApplicationPlugin
import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.kotlin.dsl.*
import java.io.ObjectStreamException
import java.io.Serializable

class _plugin : ProjectPlugin({
	apply {
		plugin<build.base._plugin>()
		plugin(kotlin("jvm"))
	}

	setUp(this)

	val pluginManager = pluginManager
	pluginManager.withPlugin("application") {
		setUpApplicationJvmArgs()
	}
	pluginManager.withPlugin("com.github.johnrengelman.shadow") {
		tasks.named<ShadowJar>(ShadowJavaPlugin.SHADOW_JAR_TASK_NAME) {
			// KLUDGE for https://github.com/johnrengelman/shadow/issues/729
			exclude("META-INF/versions/*/module-info.class")
		}
	}

	if (group != "inclusives" && name != "testing") {
		dependencies.testImplementation("inclusives:testing")
	}
})

private class JvmArgsIterable(
	private val jvmArgs: ListProperty<String>,
) : Iterable<String>, Serializable {
	private fun asList(): List<String> = jvmArgs.orNull ?: emptyList()
	override fun toString() = "JvmArgsIterable(${asList()})"
	override fun iterator() = asList().iterator()

	@Suppress("unused")
	@Throws(ObjectStreamException::class)
	private fun writeReplace(): Any = asList().toTypedArray().asList()
}

/**
 * @see build.api.dsl.accessors.jvmArgs
 */
private fun Project.setUpApplicationJvmArgs() {
	val jvmArgs = objects.listProperty<String>()

	val application = application
	(application as ExtensionAware).xs().add(typeOf(), "jvmArgs", jvmArgs)

	val jvmArgsIterable: Iterable<String> = JvmArgsIterable(jvmArgs)
	application.applicationDefaultJvmArgs = jvmArgsIterable
	afterEvaluate(fun Project.(): Unit = afterEvaluate(fun Project.() = afterEvaluate(fun(_) {
		check(application.applicationDefaultJvmArgs === jvmArgsIterable) {
			"Should not modify `application.applicationDefaultJvmArgs`\n" +
				"- Use `application.jvmArgs` (extension) instead."
		}
	})))

	// -=-

	if (isDebug) afterEvaluate {
		setUpJvmArgsForDebug(jvmArgs)
	}

	val tasks = tasks
	tasks.setUpApplicationJvmArgsForRunTasks(
		runTaskName = ApplicationPlugin.TASK_RUN_NAME,
		startScriptsTaskName = ApplicationPlugin.TASK_START_SCRIPTS_NAME,
		jvmArgs, jvmArgsIterable,
	)
	pluginManager.withPlugin("com.github.johnrengelman.shadow") {
		tasks.setUpApplicationJvmArgsForRunTasks(
			runTaskName = ShadowApplicationPlugin.SHADOW_RUN_TASK_NAME,
			startScriptsTaskName = ShadowApplicationPlugin.SHADOW_SCRIPTS_TASK_NAME,
			jvmArgs, jvmArgsIterable,
		)
	}
}

private fun TaskContainer.setUpApplicationJvmArgsForRunTasks(
	runTaskName: String,
	startScriptsTaskName: String,
	jvmArgs: ListProperty<String>,
	jvmArgsIterable: Iterable<String>,
) {
	// NOTE: It seems that `application.applicationDefaultJvmArgs` is set up via
	// `convention()` now, contrary to what we previously believed, or perhaps,
	// this was introduced to `Gradle` by mistake when `jvmArguments` was
	// introduced (as an alternative to `jvmArgs`). Moreover, the docs isn't
	// even clear about `applicationDefaultJvmArgs` being a "convention" value.
	// - See, https://github.com/gradle/gradle/pull/23924
	// - See also,
	//   - https://github.com/gradle/gradle/issues/15239
	//   - https://github.com/gradle/gradle/issues/13463#issuecomment-1468710781
	//
	// The following nullifies any issues that may come from the aforementioned
	// setup.
	named<JavaExec>(runTaskName) {
		jvmArguments.set(jvmArgs)
	}
	named<CreateStartScripts>(startScriptsTaskName) {
		defaultJvmOpts = jvmArgsIterable
	}
}

private fun setUpJvmArgsForDebug(jvmArgs: ListProperty<String>) {
	jvmArgs.add("-ea") // Also enables stacktrace recovery for kotlinx coroutines
}
