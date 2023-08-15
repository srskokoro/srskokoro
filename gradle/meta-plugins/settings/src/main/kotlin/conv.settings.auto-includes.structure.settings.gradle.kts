plugins {
	id("conv.settings.auto-includes.base")
}

autoIncludeSubProjects(rootDir, "")

fun Settings.autoIncludeSubProjects(parentProjectDir: File, parentProjectId: String) {
	parentProjectDir.list()?.forEach { name ->
		if (name.startsWith('.') || name == "build") return@forEach // Skip (just in case)

		// Include all subfolders that contain a 'build.gradle.kts' as
		// subprojects (but exclude those that look like included builds).
		val buildFile = File(parentProjectDir, "$name/build.gradle.kts")
		if (!buildFile.exists() || File(parentProjectDir, "$name/settings.gradle.kts").exists()) {
			return@forEach
		}

		val childProjectName = run<String> {
			// Incorporate convenient project directory name prefixes to affect
			// file system sorting in the IDE's project view.
			if (name.isNotEmpty()) when (name[0]) {
				'#', '+', '~' -> return@run name.substring(1)
			}
			name
		}

		val childProjectId = "$parentProjectId:$childProjectName"
		include(childProjectId) // Resolves relative to `settings.rootDir`

		if (childProjectName != name) {
			project(childProjectId).projectDir = File(parentProjectDir, name)
		}

		autoIncludeSubProjects(buildFile.parentFile, childProjectId)
	}
}
