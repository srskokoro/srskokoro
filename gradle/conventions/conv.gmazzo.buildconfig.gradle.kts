plugins {
	id("com.github.gmazzo.buildconfig")
}

// Automatically generate build config files on "gradle sync" (via IntelliJ IDEA
// or Android Studio) -- https://twitter.com/Sellmair/status/1619308362881187840
tasks.maybeCreate("prepareKotlinIdeaImport")
	.dependsOn(tasks.generateBuildConfig)
