plugins {
	id("conv.settings.auto-includes.base")

	id("conv.settings.deps")
	id("conv.settings.repositories")
}

dependencyVersionsSetup {
	useInProjects()

	// Include our centralized dependency versions
	settings.extra.getOrNull<String>(gradleProp_autoIncludesDirs_deps)?.let { prop ->
		val target = relativize(File(autoIncludesRoot, prop))

		// If we're the root, share the root 'gradle.properties'
		if (isAtAutoIncludesRoot) shareGradleProperties(target)

		includeBuild(target) // Resolves relative to `settingsDir`
	}
}
