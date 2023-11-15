@file:Suppress("PackageDirectoryMismatch")

import org.gradle.api.Project

/**
 * @see unsafeParent
 */
val Project.evaluatedParent: Project
	get() {
		val parent = parent!!
		if (!parent.state.executed) {
			// NOTE: Blocks until the parent project is evaluated.
			evaluationDependsOn(parent.path)
		}
		return parent
	}

/**
 * Returns the parent project, but unlike [evaluatedParent], it is NOT
 * guaranteed that the parent project is already evaluated.
 */
val Project.unsafeParent: Project
	get() = parent!!
