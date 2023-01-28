@file:Suppress("UnstableApiUsage")

pluginManagement {
	// Include our own custom plugins
	"gradle/plugins".let { dir ->
		for (parent in arrayOf("", "../", "../../")) {
			val target = "$parent$dir"
			if (File(rootDir, "$target/settings.gradle.kts").exists()) {
				includeBuild(target)
				break
			}
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
