package build.api.dsl

import build.api.dsl.accessors.androidOrNull
import org.gradle.api.Project

private val REGEX_NON_WORD_CHARS = Regex("\\W")

fun AndroidExtension.autoNamespace(project: Project, parentNamespace: String) {
	val namespace = buildString {
		if (parentNamespace.isNotEmpty()) {
			append(parentNamespace)
			append(".")
		}
		// Converts invalid identifier characters into underscores
		val name = project.name.replace(REGEX_NON_WORD_CHARS, "_")
		if (name.isEmpty() || name[0].lowercaseChar() !in 'a'..'z') {
			// NOTE: Individual package name parts can only start with letters.
			// See, https://developer.android.com/guide/topics/manifest/manifest-element.html#package
			append("xx_")
		}
		append(name)
	}
	this.namespace = namespace
}

fun AndroidExtension.autoNamespace(project: Project, parentAndroidExt: AndroidExtension) {
	autoNamespace(project, requireNotNull(parentAndroidExt.namespace) {
		"The specified parent `android` extension must have a `namespace` set."
	})
}

fun AndroidExtension.autoNamespace(project: Project, parentProject: Project) {
	autoNamespace(project, requireNotNull(parentProject.androidOrNull) {
		"The specified parent project must have AGP applied: $parentProject"
	})
}

fun AndroidExtension.autoNamespace(project: Project) {
	autoNamespace(project, requireNotNull(project.parent?.androidOrNull) {
		"The parent project (of the specified $project) must have AGP applied."
	})
}

// --

fun AndroidExtension.autoNamespaceOrNop(project: Project, parentNamespace: String?) {
	autoNamespace(project, parentNamespace ?: return)
}

fun AndroidExtension.autoNamespaceOrNop(project: Project, parentAndroidExt: AndroidExtension?) {
	autoNamespace(project, parentAndroidExt?.namespace ?: return)
}

fun AndroidExtension.autoNamespaceOrNop(project: Project, parentProject: Project?) {
	autoNamespace(project, parentProject?.androidOrNull?.namespace ?: return)
}

fun AndroidExtension.autoNamespaceOrNop(project: Project) {
	autoNamespace(project, project.parent?.androidOrNull?.namespace ?: return)
}
