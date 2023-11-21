plugins {
	id("build.plugins.root")
	// Necessary to avoid the plugin to be loaded multiple times in each
	// subproject's classloader -- https://youtrack.jetbrains.com/issue/KT-46200
	`kotlin-dsl` apply false
}

allprojects {
	group = "convention"
}
