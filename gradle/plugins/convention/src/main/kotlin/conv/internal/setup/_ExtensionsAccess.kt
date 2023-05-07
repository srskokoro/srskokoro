package conv.internal.setup

import conv.deps.DependencyVersions
import conv.internal.util.*
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet


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
