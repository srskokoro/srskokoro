@file:Suppress("PackageDirectoryMismatch")

import org.gradle.api.Project

val Project.evaluatedParent: Project
	get() {
		val parent = parent!!
		if (!parent.state.executed) {
			// NOTE: Blocks until the parent project is evaluated.
			evaluationDependsOn(parent.path)
		}
		return parent
	}
