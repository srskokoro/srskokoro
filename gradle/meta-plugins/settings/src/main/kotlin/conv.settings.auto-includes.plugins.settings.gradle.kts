pluginManagement {
	// Include our own custom plugins
	val target = File(autoIncludesRoot, "gradle/plugins")
	relativize(target).let {
		// If we're the root, share the root 'gradle.properties'
		if (isAtAutoIncludesRoot) shareGradleProperties(it) {
			setProperty(gradleProp_autoIncludesDirs_root, settingsDir.toRelativeString(base = target))
		}
		includeBuild(it) // Resolves relative to `settingsDir`
	}
}
plugins {
	id("conv.settings.auto-includes.base")
}
