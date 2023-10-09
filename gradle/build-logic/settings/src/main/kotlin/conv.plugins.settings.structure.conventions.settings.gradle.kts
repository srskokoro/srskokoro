pluginManagement {
	// Include our own custom plugins
	settings.extra.getOrNull<String>(gradleProp_structureDirs_conventions)?.let { prop ->
		val target = relativize(File(structureRoot, prop))

		includeBuild(target) // Resolves relative to `settingsDir`
	}
}
plugins {
	id("conv.settings.structure.base")
}
