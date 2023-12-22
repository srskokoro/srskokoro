package build.support.kt.base

import build.api.ProjectPlugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.internal.logging.slf4j.ContextAwareTaskLogger
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion as KotlinCompileVersion

/**
 * WARNING: Before making changes to this plugin, please see first the NOTE
 * provided in [build.plugins.base._plugin], as that one is expected to apply
 * this [plugin][_plugin].
 */
class _plugin : ProjectPlugin {
	override fun Project.applyPlugin() {
		apply {
			plugin<build.kt.base._plugin>()
		}
		apply_()
	}
}

private object Build {
	// NOTE: The following ensures that our convention plugins are always
	// compiled with a consistent JVM bytecode target version. Otherwise, the
	// compiled output would vary depending on the current JDK running Gradle.
	// Now, we don't want to alter the JVM toolchain, since not only that it
	// would download an entirely separate JDK, but it would also affect our
	// dependencies, e.g., it would force a specific variant of our dependencies
	// to be selected in order to conform to the current JDK (and it would
	// otherwise throw if it can't do so). We want none of that hassle when we
	// just want our target bytecode to be consistent. Additionally, not only
	// that we want consistency, we also want the output's bytecode version to
	// be as low as it can reasonably be, i.e., Java `1.8`, as Android Studio
	// currently expects that, or it'll complain stuffs like "cannot inline
	// bytecode built with JVM target <higher version>…" etc., when the build
	// isn't even complaining that.
	inline val KOTLIN_JVM_TARGET get() = JvmTarget.JVM_1_8
	const val JAVAC_RELEASE_OPT = "--release=8"
}

private fun Project.apply_() {
	tasks {
		withType<JavaCompile>().configureEach {
			options.compilerArgs.add(Build.JAVAC_RELEASE_OPT)
		}
		withType<KotlinCompile>().configureEach(fun(task) = with(task.compilerOptions) {
			task.jvmTargetValidationMode.set(JvmTargetValidationMode.IGNORE)
			jvmTarget.set(Build.KOTLIN_JVM_TARGET)

			val kotlinVersion = KotlinCompileVersion.DEFAULT
			apiVersion.set(kotlinVersion)
			languageVersion.set(kotlinVersion)

			/** @see org.gradle.kotlin.dsl.provider.KotlinDslPluginSupport.kotlinCompilerArgs */
			freeCompilerArgs.apply {
				add("-java-parameters")
				add("-Xjvm-default=all")
			}

			/** @see org.gradle.kotlin.dsl.plugins.dsl.KotlinDslCompilerPlugins */
			(task.logger as ContextAwareTaskLogger).setMessageRewriter(
				@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
				org.gradle.kotlin.dsl.plugins.dsl.ExperimentalCompilerWarningSilencer(listOf(
					"-XXLanguage:+DisableCompatibilityModeForNewInference",
					"-XXLanguage:-TypeEnhancementImprovementsInStrictMode",
				))
			)
		})
	}
}
