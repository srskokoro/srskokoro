@file:Suppress("UnstableApiUsage")

pluginManagement {
	// Include our own custom plugins
	"gradle/plugins".let { pluginsDir ->
		for (parent in arrayOf("", "../", "../../")) {
			val target = "$parent$pluginsDir"
			if (File(rootDir, "$target/settings.gradle.kts").exists().not()) continue

			// If we're the main build, share its root 'gradle.properties'
			if (parent == "") shareGradleProperties(pluginsDir)

			includeBuild(target)
			break // Done!
		}
	}
}

// Include all subfolders that contain a 'build.gradle.kts' as subprojects
rootDir.let { rootDir ->
	rootDir.list()
		?.asSequence()
		?.filter { File(rootDir, "$it/build.gradle.kts").exists() }
		?.forEach { include(it) }
}
