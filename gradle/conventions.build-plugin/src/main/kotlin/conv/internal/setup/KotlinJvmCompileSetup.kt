package conv.internal.setup

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation

internal fun setUpKotlinVersions(compilation: KotlinCompilation<*>) {
	val kotlinVersion = KotlinVersion.DEFAULT
	compilation.compilerOptions.options.apply {
		languageVersion.set(kotlinVersion)
		apiVersion.set(kotlinVersion)
	}
	// NOTE: For some reason, Android Studio isn't honoring the above set
	// compiler options and that the following is what's necessary.
	compilation.defaultSourceSet.languageSettings.apply {
		val kotlinVersionStr = kotlinVersion.version
		languageVersion = kotlinVersionStr
		apiVersion = kotlinVersionStr
	}
}

internal fun setUp(compilerOptions: KotlinJvmCompilerOptions) = with(compilerOptions) {
	// Output Java 8 default methods in interfaces -- https://kotlinlang.org/docs/java-to-kotlin-interop.html#default-methods-in-interfaces
	freeCompilerArgs.add("-Xjvm-default=all")
}
