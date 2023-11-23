pluginManagement {
	// Include our own custom plugins
	settings.extra.getOrNull<String>(gradleProp_structureDirs_plugins)?.let { prop ->
		val target = relativize(File(structureRoot, prop))

		// If we're the root, share the root 'gradle.properties'
		if (isAtStructureRoot) shareGradleProperties(target)

		includeBuild(target) // Resolves relative to `settingsDir`
	}
}
plugins {
	id("build.settings.structure.base")
}
