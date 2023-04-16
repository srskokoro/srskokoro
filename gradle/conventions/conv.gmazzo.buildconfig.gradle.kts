import conv.util.*

plugins {
	id("com.github.gmazzo.buildconfig")
}

buildConfig.let {
	val xs = (it as ExtensionAware).extensions
	it.sourceSets.all { this asExtensionIn xs }
}

// Automatically generate build config files on "gradle sync" (via IntelliJ IDEA
// or Android Studio) -- https://twitter.com/Sellmair/status/1619308362881187840
tasks.let {
	it.maybeCreate("prepareKotlinIdeaImport")
		.dependsOn(it.generateBuildConfig)
}
