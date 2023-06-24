package conv.sub.android

import conv.internal.setup.*
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.extra

private const val conv_sub_android__pluginId = "conv.sub.android"

fun AndroidExtension.autoNamespace(parentNamespaceSource: Project) = autoNamespace(parentNamespaceSource) {
	error("The specified project doesn't have AGP applied: $parentNamespaceSource")
}

internal inline fun AndroidExtension.autoNamespace(parentNamespaceSource: Project, onFailure: () -> Unit) {
	val parentAndroidExt = parentNamespaceSource.extensions
		.findByName("android") as? AndroidExtension
		?: return onFailure()

	autoNamespace(parentAndroidExt.namespace)
}

internal const val autoNamespace_suffix__name = "autoNamespace_suffix"

fun AndroidExtension.autoNamespace(parentNamespace: String?) {
	val suffix = (this as ExtensionAware).extra[autoNamespace_suffix__name] as? String
		?: error("Must first apply plugin: \"$conv_sub_android__pluginId\"")

	namespace = buildString {
		if (!parentNamespace.isNullOrEmpty()) {
			append(parentNamespace)
			append(".")
		}
		append(suffix)
	}
}
