pluginManagement {
	// Include our own custom plugins
	settings.extra.getOrNull<String>(gradleProp_autoIncludesDirs_plugins)?.let { prop ->
		val target = relativize(File(autoIncludesRoot, prop))

		// If we're the root, share the root 'gradle.properties'
		if (isAtAutoIncludesRoot) shareGradleProperties(target)

		includeBuild(target) // Resolves relative to `settingsDir`
	}
}
plugins {
	id("conv.settings.auto-includes.base")
}
