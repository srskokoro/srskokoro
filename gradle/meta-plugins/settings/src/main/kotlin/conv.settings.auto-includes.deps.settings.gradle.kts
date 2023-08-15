plugins {
	id("conv.settings.auto-includes.base")
	id("conv.settings.deps")
	id("conv.settings.repositories")
}

dependencyVersionsSetup {
	useInProjects()

	// Include our centralized dependency versions
	relativize(File(gitRootDir!!, "gradle/dependencies")).let {
		// If we're the root, share the root 'gradle.properties'
		if (isAtGitRoot) shareGradleProperties(it)

		includeBuild(it) // Resolves relative to `settingsDir`
	}
}
