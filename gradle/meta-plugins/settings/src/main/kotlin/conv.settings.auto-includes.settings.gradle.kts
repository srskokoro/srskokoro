pluginManagement {
	// Include our own custom plugins
	relativize(File(gitRootDir!!, "gradle/plugins")).let {
		// If we're the root, share the root 'gradle.properties'
		if (isAtGitRoot) shareGradleProperties(it)

		includeBuild(it) // Resolves relative to `settingsDir`
	}
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

autoIncludeSubProjects(rootDir, "")

fun Settings.autoIncludeSubProjects(parentProjectDir: File, parentProjectId: String) {
	parentProjectDir.list()?.forEach {
		if (it.startsWith('.') || it == "build") return@forEach // Skip (just in case)
		// Include all subfolders that contain a 'build.gradle.kts' as
		// subprojects (but exclude those that look like included builds).
		val buildFile = File(parentProjectDir, "$it/build.gradle.kts")
		if (buildFile.exists() && !File(parentProjectDir, "$it/settings.gradle.kts").exists()) {
			val childProjectId = "$parentProjectId:$it"
			include(childProjectId) // Resolves relative to `settings.rootDir`
			autoIncludeSubProjects(buildFile.parentFile, childProjectId)
		}
	}
}
