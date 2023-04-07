@file:Suppress("UnstableApiUsage")

pluginManagement {
	// Include our own custom plugins
	relativize(File(rootSettingsDir, "gradle/plugins")).let {
		// If we're the main build, share its root 'gradle.properties'
		if (isRootBuild) shareGradleProperties(it)

		includeBuild(it) // Resolves relative to `settingsDir`
	}
}

dependencyVersionsSetup {
	useInProjects()

	// Include our centralized dependency versions
	relativize(File(rootSettingsDir, "gradle/dependencies")).let {
		// If we're the main build, share its root 'gradle.properties'
		if (isRootBuild) shareGradleProperties(it)

		includeBuild(it) // Resolves relative to `settingsDir`
	}
}

// Include all subfolders that contain a 'build.gradle.kts' as subprojects (but
// exclude those that look like included builds).
rootDir.let { rootDir ->
	rootDir.list()?.asSequence()?.filter {
		File(rootDir, "$it/build.gradle.kts").exists() &&
		!File(rootDir, "$it/settings.gradle.kts").exists()
	}?.forEach {
		include(it) // Resolves relative to `rootDir`
	}
}
