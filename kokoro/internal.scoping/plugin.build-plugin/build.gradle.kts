import build.api.dsl.*
import build.api.dsl.accessors.compileOnlyTestImpl

plugins {
	id("build.plugins")
	id("com.github.gmazzo.buildconfig")
}

group = extra["kokoro.group"] as String
base.archivesName = extra["kokoro.internal.scoping.plugin.artifact"] as String

val NAMESPACE = extra["kokoro.internal.scoping.ns"] as String

buildConfig {
	packageName(NAMESPACE)

	useKotlinOutput {
		topLevelConstants = true
		internalVisibility = true
	}

	buildConfigField("COMPILER_ARTIFACT_GROUP", provider { projectThis.group.toString() })
	buildConfigField("COMPILER_ARTIFACT_NAME", provider { projectThis.extra["kokoro.internal.scoping.compiler.artifact"] as String })
	buildConfigField("COMPILER_PLUGIN_ID", provider { "$NAMESPACE.compiler" })

	buildConfigField("RUNTIME_ARTIFACT_COORDINATE", provider { "${projectThis.group}:${projectThis.extra["kokoro.internal.scoping.artifact"]}" })
}

dependencies {
	compileOnlyTestImpl(kotlin("gradle-plugin"))
	implementation("conventions:support.kotlin-gradle-plugin") {
		exclude("org.jetbrains.kotlin", "kotlin-gradle-plugin")
	}
}
