package build.conventions.internal

import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
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
	const val JAVAC_RELEASE_OPT = "--release=8"
}

/**
 * Sets up [project] so that it may be used for both the app and build logic.
 */
fun InternalConventions.ensureMultipurpose(project: Project): Unit = with(project) {
	tasks.run {
		withType<JavaCompile>().configureEach {
			options.compilerArgs.add(Build.JAVAC_RELEASE_OPT)
		}
		withType<KotlinCompile>().configureEach(fun(task) = with(task.compilerOptions) {
			task.jvmTargetValidationMode.set(JvmTargetValidationMode.IGNORE)
			jvmTarget.set(Build.KOTLIN_JVM_TARGET)

			val kotlinVersion = KotlinCompileVersion.DEFAULT
			apiVersion.set(kotlinVersion)
			languageVersion.set(kotlinVersion)

			ensureMultipurpose(this)
		})
	}

	dependencies.run {
		val bom = enforcedPlatform(embeddedKotlin("bom"))
		val stdlib = embeddedKotlin("stdlib")

		val kotlin = project.extensions.getByName("kotlin")
		// NOTE: The following will prevent `kotlin("stdlib")` from being added
		// automatically by KGP -- see, https://kotlinlang.org/docs/gradle-configure-project.html#dependency-on-the-standard-library
		if (kotlin !is KotlinMultiplatformExtension) {
			compileOnlyTestImpl(bom)
			compileOnlyTestImpl(stdlib)
		} else {
			redirectCompileOnlyForNative(kotlin)
			commonMainCompileOnlyTestImpl(bom)
			commonMainCompileOnlyTestImpl(stdlib)
		}
	}
}

// KLUDGE K/N doesn't support `compileOnly` (at the moment)
// - See, https://youtrack.jetbrains.com/issue/KT-64109
/** @see org.jetbrains.kotlin.gradle.plugin.KotlinNativeTargetConfigurator.warnAboutIncorrectDependencies */
fun redirectCompileOnlyForNative(kotlin: KotlinMultiplatformExtension) {
	kotlin.targets.withType<KotlinNativeTarget>().configureEach {
		compilations.configureEach {
			val cs = project.configurations
			val compileOnly = cs.getByName(compileOnlyConfigurationName)
			val implementation = cs.getByName(implementationConfigurationName)
			implementation.setExtendsFrom(compileOnly.extendsFrom)
			compileOnly.setExtendsFrom(emptySet())
		}
	}
}

/**
 * @see org.gradle.kotlin.dsl.provider.KotlinDslPluginSupport.kotlinCompilerArgs
 */
private fun ensureMultipurpose(compilerOptions: KotlinJvmCompilerOptions) {
	compilerOptions.freeCompilerArgs.apply {
		add("-java-parameters")
		add("-Xjvm-default=all")
		add("-Xjsr305=strict")
		add("-Xsam-conversions=class")
	}
}
