package conv.internal.setup

import conv.deps.DependencyVersions
import conv.internal.support.unsafeCast
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet


internal val Project.baseExt
	get() = extensions.getByName("base") as BasePluginExtension

internal val Project.androidExt
	get() = extensions.getByName("android") as AndroidExtension

/** @see kotlinMppExt */
internal val Project.kotlinExt
	get() = extensions.getByName("kotlin") as KotlinProjectExtension

/** @see kotlinExt */
internal val Project.kotlinMppExt
	get() = extensions.getByName("kotlin") as KotlinMultiplatformExtension


internal val Project.deps
	get() = extensions.getByName("deps") as DependencyVersions


internal val Project.sourceSets
	get() = extensions.getByName("sourceSets") as SourceSetContainer

internal var Project.kotlinSourceSets
	get() = extensions.getByName("kotlinSourceSets").unsafeCast<NamedDomainObjectContainer<KotlinSourceSet>>()
	set(value) = extensions.add("kotlinSourceSets", value)

internal inline fun Project.kotlinSourceSets(
	configure: NamedDomainObjectContainer<KotlinSourceSet>.() -> Unit
) = configure(kotlinSourceSets)
