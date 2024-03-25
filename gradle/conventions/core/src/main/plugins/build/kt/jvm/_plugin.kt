package build.kt.jvm

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.application
import build.api.dsl.accessors.testImplementation
import build.setUp
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.base._plugin>()
		plugin(kotlin("jvm"))
	}

	setUp(this)

	pluginManager.withPlugin("application") {
		setUpApplicationJvmArgs()
	}

	if (group != "inclusives" && name != "testing") {
		dependencies.testImplementation("inclusives:testing")
	}
})

/**
 * @see build.api.dsl.accessors.jvmArgs
 */
private fun Project.setUpApplicationJvmArgs() {
	val jvmArgs = objects.listProperty<String>()

	val application = application
	(application as ExtensionAware).xs().add(typeOf(), "jvmArgs", jvmArgs)

	val jvmArgsIterable = Iterable {
		jvmArgs.orNull?.iterator()
			?: emptyList<String>().iterator()
	}

	application.applicationDefaultJvmArgs = jvmArgsIterable
	afterEvaluate(fun Project.(): Unit = afterEvaluate(fun Project.() = afterEvaluate(fun(_) {
		check(application.applicationDefaultJvmArgs === jvmArgsIterable) {
			"Should not modify `application.applicationDefaultJvmArgs`\n" +
				"- Use `application.jvmArgs` (extension) instead."
		}
	})))
}
