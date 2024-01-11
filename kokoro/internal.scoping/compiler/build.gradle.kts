plugins {
	id("build.kt.jvm")
}

group = extra["kokoro.group"] as String
base.archivesName = "kokoro-internal-scoping-compiler"

tasks.compileKotlin {
	compilerOptions.freeCompilerArgs.run {
		add("-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
	}
}

dependencies {
	compileOnly(kotlin("compiler-embeddable"))
}
