package conv.internal.setup

import build.deps.DependencyVersions
import build.internal.support.unsafeCast
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet


val Project.baseExt
	get() = extensions.getByName("base") as BasePluginExtension

val Project.baseExtOrNull
	get() = extensions.findByName("base") as? BasePluginExtension

val Project.androidExt
	get() = extensions.getByName("android") as AndroidExtension

val Project.androidExtOrNull
	get() = extensions.findByName("android") as? AndroidExtension

/** @see kotlinMppExt */
val Project.kotlinExt
	get() = extensions.getByName("kotlin") as KotlinProjectExtension

/** @see kotlinExt */
val Project.kotlinMppExt
	get() = extensions.getByName("kotlin") as KotlinMultiplatformExtension


val Project.deps
	// NOTE: Given that the extension below is set via the `settings` script, it
	// will be null if Gradle simply evaluated a fake project in order to
	// generate type-safe model accessors for precompiled script plugins to use.
	//
	// NOTE: The cast below throws on non-null incompatible types as intended.
	get() = extensions.findByName("deps") as DependencyVersions?


val Project.sourceSets
	get() = extensions.getByName("sourceSets") as SourceSetContainer

var Project.kotlinSourceSets
	get() = extensions.getByName("kotlinSourceSets").unsafeCast<NamedDomainObjectContainer<KotlinSourceSet>>()
	internal set(value) = extensions.add("kotlinSourceSets", value)

inline fun Project.kotlinSourceSets(
	configure: NamedDomainObjectContainer<KotlinSourceSet>.() -> Unit
) = configure(kotlinSourceSets)
