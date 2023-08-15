plugins {
	id("conv.settings.auto-includes.base")
	id("conv.settings.deps")
	id("conv.settings.repositories")
}

dependencyVersionsSetup {
	useInProjects()

	// Include our centralized dependency versions
	val target = File(autoIncludesRoot, "gradle/dependencies")
	relativize(target).let {
		// If we're the root, share the root 'gradle.properties'
		if (isAtAutoIncludesRoot) shareGradleProperties(it) {
			setProperty(gradleProp_autoIncludesDirs_root, settingsDir.toRelativeString(base = target))
		}
		includeBuild(it) // Resolves relative to `settingsDir`
	}
}
