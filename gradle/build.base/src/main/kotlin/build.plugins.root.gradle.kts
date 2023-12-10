plugins {
	id("build.support.root")

	// Necessary to avoid the plugin to be loaded multiple times in each
	// subproject's classloader -- https://youtrack.jetbrains.com/issue/KT-46200
	id("org.gradle.kotlin.kotlin-dsl") apply false
}
