plugins {
	id("conv.settings.auto-includes.base")
	id("conv.settings.deps")
	id("conv.settings.repositories")
}

dependencyVersionsSetup {
	useInProjects()

	// Include our centralized dependency versions
	relativize(File(autoIncludesRoot, settings.extra[gradleProp_autoIncludesDirs_deps] as String)).let {
		// If we're the root, share the root 'gradle.properties'
		if (isAtAutoIncludesRoot) shareGradleProperties(it)

		includeBuild(it) // Resolves relative to `settingsDir`
	}
}
