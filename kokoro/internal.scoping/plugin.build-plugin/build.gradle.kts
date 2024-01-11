plugins {
	id("build.plugins")
	id("com.github.gmazzo.buildconfig")
}

group = extra["kokoro.group"] as String
base.archivesName = "${Build.ARTIFACT_NAME_PREFIX}plugin"

private object Build {
	const val PLUGIN_PACKAGE = "kokoro.internal.scoping"
	const val ARTIFACT_NAME_PREFIX = "kokoro-internal-scoping-"
}

@Suppress("INLINE_FROM_HIGHER_PLATFORM")
buildConfig {
	packageName(Build.PLUGIN_PACKAGE)
	useKotlinOutput {
		topLevelConstants = true
		internalVisibility = true
	}
	buildConfigField("COMPILER_ARTIFACT_GROUP", provider { group.toString() })
	buildConfigField("COMPILER_ARTIFACT_NAME", provider { "${Build.ARTIFACT_NAME_PREFIX}compiler" })
	buildConfigField("COMPILER_PLUGIN_ID", provider { "${Build.PLUGIN_PACKAGE}.compiler" })
}

dependencies {
	compileOnly(kotlin("gradle-plugin"))
}
