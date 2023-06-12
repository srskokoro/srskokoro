package conv.internal.setup

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions

internal fun setUp(compilerOptions: KotlinJvmCompilerOptions) = with(compilerOptions) {
	// Output Java 8 default methods in interfaces -- https://kotlinlang.org/docs/java-to-kotlin-interop.html#default-methods-in-interfaces
	freeCompilerArgs.add("-Xjvm-default=all")
}
