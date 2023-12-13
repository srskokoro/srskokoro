package build.plugins

import build.api.dsl.model.compileOnlyTestImpl
import build.api.dsl.model.implementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.support.expectedKotlinDslPluginsVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

abstract class _plugin : Plugin<Project> {
	override fun apply(project: Project) {
		project.apply {
			plugin("java-gradle-plugin")
			plugin("org.gradle.kotlin.kotlin-dsl.base")
		}
		project.pluginApply()
	}
}

internal object Build {
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
	val KOTLIN_JVM_TARGET = JvmTarget.JVM_1_8
	const val JAVAC_RELEASE_OPT = "--release=8"
}

internal fun Project.pluginApply() {
	installPluginsAutoRegistrant()

	tasks {
		withType<JavaCompile>().configureEach {
			options.compilerArgs.add(Build.JAVAC_RELEASE_OPT)
		}
		withType<KotlinCompile>().configureEach(fun(task) = with(task.compilerOptions) {
			task.jvmTargetValidationMode.set(JvmTargetValidationMode.IGNORE)
			jvmTarget.set(Build.KOTLIN_JVM_TARGET)

			val kotlinVersion = KotlinVersion.DEFAULT
			apiVersion.set(kotlinVersion)
			languageVersion.set(kotlinVersion)
			freeCompilerArgs.add("-Xjvm-default=all")
		})
	}

	dependencies {
		compileOnlyTestImpl(embeddedKotlin("gradle-plugin"))
		implementation("org.gradle.kotlin:gradle-kotlin-dsl-plugins:$expectedKotlinDslPluginsVersion")
	}
}
