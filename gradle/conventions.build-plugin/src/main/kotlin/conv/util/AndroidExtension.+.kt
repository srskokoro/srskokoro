package conv.util

import conv.internal.setup.*
import org.gradle.api.Project

private val REGEX_NON_WORD_CHARS = Regex("\\W")

fun AndroidExtension.autoNamespace(project: Project, parentNamespace: String) {
	val namespace = buildString {
		if (parentNamespace.isNotEmpty()) {
			append(parentNamespace)
			append(".")
		}
		val name = project.name
		if (name.isEmpty() || !name[0].isJavaIdentifierStart()) {
			append('_')
		}
		// Converts invalid identifier characters into underscores
		append(name.replace(REGEX_NON_WORD_CHARS, "_"))
	}
	this.namespace = namespace
}

fun AndroidExtension.autoNamespace(project: Project, parentAndroidExt: AndroidExtension) {
	autoNamespace(project, requireNotNull(parentAndroidExt.namespace) {
		"The specified parent `android` extension must have a `namespace` set."
	})
}

fun AndroidExtension.autoNamespace(project: Project, parentProject: Project) {
	autoNamespace(project, requireNotNull(parentProject.androidExtOrNull) {
		"The specified parent project must have AGP applied: $parentProject"
	})
}

fun AndroidExtension.autoNamespace(project: Project) {
	autoNamespace(project, requireNotNull(project.parent?.androidExtOrNull) {
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
	autoNamespace(project, parentProject?.androidExtOrNull?.namespace ?: return)
}

fun AndroidExtension.autoNamespaceOrNop(project: Project) {
	autoNamespace(project, project.parent?.androidExtOrNull?.namespace ?: return)
}
