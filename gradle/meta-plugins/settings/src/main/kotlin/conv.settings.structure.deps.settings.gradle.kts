plugins {
	id("conv.settings.structure.base")

	id("conv.settings.deps")
	id("conv.settings.repositories")
}

dependencyVersionsSetup {
	useInProjects()

	// Include our centralized dependency versions
	settings.extra.getOrNull<String>(gradleProp_structureDirs_deps)?.let { prop ->
		val target = relativize(File(structureRoot, prop))

		// If we're the root, share the root 'gradle.properties'
		if (isAtStructureRoot) shareGradleProperties(target)

		includeBuild(target) // Resolves relative to `settingsDir`
	}
}
