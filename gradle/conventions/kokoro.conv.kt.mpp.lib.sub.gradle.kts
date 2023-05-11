plugins {
	id("kokoro.conv.kt.mpp.lib")
}

run<Unit> {
	val parent = parent ?: error("Must have a parent project")
	evaluationDependsOn(parent.path)

	val REQUIRED_PARENT_PLUGIN = "kokoro.conv.kt.mpp.lib"
	check(parent.plugins.hasPlugin(REQUIRED_PARENT_PLUGIN)) {
		"Expected plugin in parent project not found: $REQUIRED_PARENT_PLUGIN"
	}

	android.namespace = buildString {
		val parentNamespace = parent.android.namespace
		if (!parentNamespace.isNullOrEmpty()) {
			append(parentNamespace)
			append(".")
		}
		// Converts from kebab case to snake case
		append(projectDir.name.replace('-', '_'))
		// ^ NOTE: We used the project directory's name so that we have more
		// freedom when changing the project name into something more complex,
		// e.g., to avoid the issue described in, https://github.com/gradle/gradle/issues/847#issuecomment-1205001575
	}

	path.replace(':', '!').let {
		// Prevents conflicts when generating the archives, especially for tasks
		// that output an installation distribution where all the archives are
		// placed under the same directory.
		base.archivesName.set(it)
	}
}
