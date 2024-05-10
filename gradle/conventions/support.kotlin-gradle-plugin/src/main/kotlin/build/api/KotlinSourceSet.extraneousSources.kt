package build.api

import build.api.dsl.*
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

private const val XS_extraneousSources = "extraneousSources"

val KotlinSourceSet.extraneousSources: Map<String, SourceDirectorySet>
	get() = xs().getOrElse(XS_extraneousSources) { emptyMap() }

fun KotlinSourceSet.addExtraneousSource(extensionName: String, source: SourceDirectorySet) {
	val xs = xs()
	val sources = xs.getOrAdd(XS_extraneousSources) { LinkedHashMap<String, SourceDirectorySet>() }

	sources.putIfAbsent(extensionName, source)
	xs.add<SourceDirectorySet>(extensionName, source) // May fail and throw
}

@Suppress("NOTHING_TO_INLINE")
inline fun KotlinSourceSet.getExtraneousSource(extensionName: String): SourceDirectorySet = x(extensionName)

@Suppress("NOTHING_TO_INLINE")
inline fun KotlinSourceSet.getExtraneousSourceOrNull(extensionName: String): SourceDirectorySet? = xs().getOrNull(extensionName)

// --

@Suppress("NOTHING_TO_INLINE")
inline fun ObjectFactory.addExtraneousSourceTo(
	sourceSet: KotlinSourceSet,
	extensionName: String,
	displayName: String = extensionName,
): SourceDirectorySet {
	val source = sourceDirectorySet(extensionName, displayName)
	sourceSet.addExtraneousSource(extensionName, source)
	return source
}

@Suppress("NOTHING_TO_INLINE")
inline fun ObjectFactory.addExtraneousSourceTo(
	sourceSet: KotlinSourceSet,
	extensionName: String,
	displayName: String = extensionName,
	configure: KotlinSourceSet.(SourceDirectorySet) -> Unit,
): SourceDirectorySet {
	val source = addExtraneousSourceTo(
		sourceSet,
		extensionName = extensionName,
		displayName,
	)
	configure(sourceSet, source)
	return source
}

@Suppress("NOTHING_TO_INLINE")
inline fun KotlinSourceSet.addExtraneousSource(
	extensionName: String,
	displayName: String = extensionName,
) = project.objects.addExtraneousSourceTo(
	this,
	extensionName = extensionName,
	displayName,
)

@Suppress("NOTHING_TO_INLINE")
inline fun KotlinSourceSet.addExtraneousSource(
	extensionName: String,
	displayName: String = extensionName,
	configure: KotlinSourceSet.(SourceDirectorySet) -> Unit,
) = project.objects.addExtraneousSourceTo(
	this,
	extensionName = extensionName,
	displayName,
	configure = configure,
)
