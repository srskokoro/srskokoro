package build.root.base

import build.api.ProjectPlugin
import build.api.dsl.*
import org.gradle.api.InvalidUserDataException

class _plugin : ProjectPlugin({
	check(parent == null) { "Must only be applied to the root project" }

	// NOTE: Given that we set up the build directory to a custom location, we
	// must ensure that we are the first plugin applied, that is, before all
	// other plugins that interact with the build directory. We can't however
	// simply check if there are any plugins already applied, since Gradle or
	// the IDE might apply its own plugins before us. So we simply check for
	// `lifecycle-base`, which is often applied by plugins (often implicitly)
	// that interact with the build directory.
	// - See also, https://github.com/gradle/gradle/issues/15664
	// TODO Consider not throwing and simply let consumers apply this plugin
	//  however they want, in whatever order they want.
	if (pluginManager.hasPlugin("lifecycle-base")) throw InvalidUserDataException(
		"Plugin \"${_plugin::class.java.packageName}\" should be applied first (before everything else)"
	) else allprojects {
		layout.buildDirectory.set(file(".build"))
	}
	apply {
		plugin("lifecycle-base")
	}

	afterEvaluate {
		val tasks = tasks

		tasks.named("check") { dependOnSameTaskFromSubProjects() }
		tasks.named("clean") { dependOnSameTaskFromSubProjects() }
	}
})
