pluginManagement {
	// Include our own custom plugins
	relativize(File(autoIncludesRoot, "gradle/plugins")).let {
		// If we're the root, share the root 'gradle.properties'
		if (isAtAutoIncludesRoot) shareGradleProperties(it)

		includeBuild(it) // Resolves relative to `settingsDir`
	}
}
plugins {
	id("conv.settings.auto-includes.base")
}
