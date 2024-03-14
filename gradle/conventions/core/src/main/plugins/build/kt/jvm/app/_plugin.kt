package build.kt.jvm.app

import build.api.ProjectPlugin
import build.api.dsl.*
import com.github.jengelman.gradle.plugins.shadow.ShadowApplicationPlugin
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin<build.kt.jvm._plugin>()
		plugin("application")
		plugin("com.github.johnrengelman.shadow")
	}

	val tasks = tasks

	// KLUDGE to force the inclusion of `application.applicationDefaultJvmArgs`,
	//  since `Gradle` seems to set it up via `jvmArguments.convention()` at the
	//  moment.
	tasks.named<JavaExec>(ApplicationPlugin.TASK_RUN_NAME) {
		jvmArgs = jvmArgs
	}
	tasks.named<JavaExec>(ShadowApplicationPlugin.SHADOW_RUN_TASK_NAME) {
		jvmArgs = jvmArgs
	}
	// NOTE: It seems that `application.applicationDefaultJvmArgs` is set up via
	// `convention()` now, contrary to what we previously believed, or perhaps,
	// this was introduced to `Gradle` by mistake when `jvmArguments` was
	// introduced (as an alternative to `jvmArgs`). Moreover, the docs isn't
	// even clear about `applicationDefaultJvmArgs` being a "convention" value.
	// - See, https://github.com/gradle/gradle/pull/23924
	// - See also,
	//   - https://github.com/gradle/gradle/issues/15239
	//   - https://github.com/gradle/gradle/issues/13463#issuecomment-1468710781

	// -=-

	tasks.withType<JavaExec>().configureEach {
		if (this.project.isDebug) {
			jvmArgs(mutableListOf<String>().apply(::setUpJvmArgsForDebug))
		}
	}
	tasks.withType<CreateStartScripts>().configureEach {
		if (this.project.isDebug) {
			defaultJvmOpts = mutableListOf<String>().apply {
				defaultJvmOpts?.let(::addAll)
				setUpJvmArgsForDebug(this)
			}
		}
	}
})

private fun setUpJvmArgsForDebug(jvmArgs: MutableList<String>) {
	jvmArgs.add("-ea") // Also enables stacktrace recovery for kotlinx coroutines
}
