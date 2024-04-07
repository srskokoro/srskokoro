package build.foundation

import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion as KotlinCompileVersion

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
}

/**
 * Sets up [project] so that it may be used for both the app and build logic.
 */
fun BuildFoundation.setUpAsBuildInclusive(project: Project): Unit = with(project) {
	tasks.run {
		withType<KotlinCompile>().configureEach(fun(task) = with(task.compilerOptions) {
			task.jvmTargetValidationMode.set(JvmTargetValidationMode.IGNORE)
			jvmTarget.set(Build.KOTLIN_JVM_TARGET)

			val kotlinVersion = KotlinCompileVersion.DEFAULT
			apiVersion.set(kotlinVersion)
			languageVersion.set(kotlinVersion)

			setUpAsBuildInclusive(this)
		})
	}

	// NOTE: KGP will automatically add `kotlin-stdlib` as an `implementation`
	// dependency. Ideally, we would add that manually ourselves via `compileOnly`,
	// but K/N doesn't support that (at the moment), and a dependency could
	// easily add a runtime dependency on `kotlin-stdlib`. Thus, let's just
	// force `kotlin-stdlib` to have a consistent version that we expect, by
	// configuring the `resolutionStrategy` of each configuration.
	configurations.configureEach {
		resolutionStrategy {
			// NOTE: All gradle plugins are forced by Gradle to use `embeddedKotlinVersion`
			// as the version for `kotlin-stdlib`. We manually enforce that here
			// so that we can't accidentally depend on a construct that wouldn't
			// work when finally consumed by Gradle. Unlike "strict" versions,
			// the consumer can easily override whatever version is in here.
			force("org.jetbrains.kotlin:kotlin-stdlib:$embeddedKotlinVersion")
			force("org.jetbrains.kotlin:kotlin-reflect:$embeddedKotlinVersion")
		}
	}
}

/**
 * @see org.gradle.kotlin.dsl.provider.KotlinDslPluginSupport.kotlinCompilerArgs
 */
private fun setUpAsBuildInclusive(compilerOptions: KotlinJvmCompilerOptions) {
	compilerOptions.freeCompilerArgs.apply {
		add("-java-parameters")
		add("-Xjvm-default=all")
		add("-Xjsr305=strict")
		add("-Xsam-conversions=class")
	}
}
