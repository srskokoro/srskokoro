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

	// Prevents conflicts when generating the archives, especially for tasks
	// that output an installation distribution where all the archives are
	// placed under the same directory. See also, https://github.com/gradle/gradle/issues/847#issuecomment-1205001575
	base.archivesName.convention(parentBaseArchivesName.map { "$it\$$name" })
}
