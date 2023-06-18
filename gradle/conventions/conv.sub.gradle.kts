plugins {
	base
}

run<Unit> {
	val parent = parent ?: error("Must have a parent project")
	evaluationDependsOn(parent.path)

	val parentBaseArchivesName = try {
		parent.base
	} catch (ex: Throwable) {
		val REQUIRED_PARENT_PLUGIN = "base"
		check(parent.pluginManager.hasPlugin(REQUIRED_PARENT_PLUGIN)) {
			"Expected plugin in parent project not found: $REQUIRED_PARENT_PLUGIN"
		}
		throw ex
	}.archivesName

	// NOTE: We'll use the project directory's name, instead of the project
	// name, so that we have more freedom in changing the project name into
	// something more complex, e.g., to avoid the issue described in,
	// https://github.com/gradle/gradle/issues/847#issuecomment-1205001575
	val projectDirName = projectDir.name

	// Prevents conflicts when generating the archives, especially for tasks
	// that output an installation distribution where all the archives are
	// placed under the same directory.
	base.archivesName.convention(parentBaseArchivesName.map { "$it!$projectDirName" })
}
