import build.api.dsl.*
import build.api.dsl.accessors.compileOnlyTestImpl

plugins {
	id("build.plugins")
	id("com.github.gmazzo.buildconfig")
}

group = extra["kokoro.group"] as String

buildConfig {
	packageName("kokoro.internal.scoping")

	useKotlinOutput {
		topLevelConstants = true
		internalVisibility = true
	}

	buildConfigField("COMPILER_ARTIFACT_GROUP", provider { projectThis.group.toString() })
	buildConfigField("COMPILER_ARTIFACT_NAME", "kokoro-internal-scoping-compiler")
	buildConfigField("COMPILER_PLUGIN_ID", "kokoro.internal.scoping.compiler")

	buildConfigField("RUNTIME_ARTIFACT_COORDINATE", provider { "${projectThis.group}:kokoro-internal-scoping" })
}

dependencies {
	compileOnlyTestImpl(kotlin("gradle-plugin"))
	implementation("conventions:support.kotlin-gradle-plugin") {
		exclude("org.jetbrains.kotlin", "kotlin-gradle-plugin")
	}
}
