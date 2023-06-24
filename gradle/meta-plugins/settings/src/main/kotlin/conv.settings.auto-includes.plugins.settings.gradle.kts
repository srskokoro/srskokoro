pluginManagement {
	// Include our own custom plugins
	relativize(File(gitRootDir!!, "gradle/plugins")).let {
		// If we're the root, share the root 'gradle.properties'
		if (isAtGitRoot) shareGradleProperties(it)

		includeBuild(it) // Resolves relative to `settingsDir`
	}
}
