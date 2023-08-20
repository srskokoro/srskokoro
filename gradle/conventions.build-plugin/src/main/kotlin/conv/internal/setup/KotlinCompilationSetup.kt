package conv.internal.setup

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation

internal fun setUpKotlinVersions(compilation: KotlinCompilation<*>) {
	val ver = KotlinVersion.DEFAULT
	compilation.compilerOptions.options.apply {
		languageVersion.set(ver)
		apiVersion.set(ver)
	}
	// NOTE: For some reason, Android Studio isn't honoring the above set
	// compiler options and that the following is what's necessary.
	compilation.defaultSourceSet.languageSettings.apply {
		val verStr = ver.version
		languageVersion = verStr
		apiVersion = verStr
	}
}

internal fun setUp(compilerOptions: KotlinJvmCompilerOptions) = with(compilerOptions) {
	// Output Java 8 default methods in interfaces -- https://kotlinlang.org/docs/java-to-kotlin-interop.html#default-methods-in-interfaces
	freeCompilerArgs.add("-Xjvm-default=all")
}
