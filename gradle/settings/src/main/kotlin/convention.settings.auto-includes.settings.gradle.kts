@file:Suppress("UnstableApiUsage")

pluginManagement {
	// Include our own custom plugins
	if (File(rootDir, "gradle/plugins").exists()) {
		includeBuild("gradle/plugins")
	}
	// If not the main build, it might be located next to the current build
	if (File(rootDir, "../plugins").exists()) {
		includeBuild("../plugins")
	}
}

// Include all subfolders that contain a 'build.gradle.kts' as subprojects
rootDir.let { rootDir ->
	rootDir.list()
		?.asSequence()
		?.filter { File(rootDir, "$it/build.gradle.kts").exists() }
		?.forEach { include(it) }
}
