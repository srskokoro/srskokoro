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

// Include all subfolders that contain a 'build.gradle.kts' as subprojects (but
// exclude those that contain a 'settings.gradle.kts').
rootDir.let { rootDir ->
	rootDir.list()?.asSequence()?.filter {
		File(rootDir, "$it/build.gradle.kts").exists() &&
			!File(rootDir, "$it/settings.gradle.kts").exists()
	}?.forEach { include(it) }
}
