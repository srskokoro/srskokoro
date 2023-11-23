@file:Suppress("PackageDirectoryMismatch")

import build.internal.support.unsafeCast
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.add
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

private const val XS_extraneousSources = "extraneousSources"

val KotlinSourceSet.extraneousSources: Map<String, SourceDirectorySet>
	get() = (this as ExtensionAware).extensions.let { xs ->
		// NOTE: The cast below throws on non-null incompatible types (as intended).
		xs.findByName(XS_extraneousSources).unsafeCast() ?: emptyMap()
	}

fun KotlinSourceSet.addExtraneousSource(extensionName: String, source: SourceDirectorySet) {
	val xs = (this as ExtensionAware).extensions

	// NOTE: The cast below throws on non-null incompatible types (as intended).
	val sources = xs.findByName(XS_extraneousSources).unsafeCast() ?: LinkedHashMap<String, SourceDirectorySet>()
		.also { xs.add<Any>(XS_extraneousSources, it) }

	sources[extensionName] = source
	xs.add<SourceDirectorySet>(extensionName, source)
}

@Suppress("NOTHING_TO_INLINE")
inline fun KotlinSourceSet.getExtraneousSource(extensionName: String) =
	(this as ExtensionAware).extensions.getByName(extensionName) as SourceDirectorySet

@Suppress("NOTHING_TO_INLINE")
inline fun KotlinSourceSet.getExtraneousSourceOrNull(extensionName: String) =
	// NOTE: The cast below throws on non-null incompatible types (as intended).
	(this as ExtensionAware).extensions.findByName(extensionName) as SourceDirectorySet?
