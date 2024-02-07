package org.gradle.kotlin.dsl

import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import org.gradle.api.NamedDomainObjectContainer

/**
 * Necessary because (at the moment) Gradle's Kotlin DSL doesn't yet respect
 * "use-site variance / type projections" when generating type-safe model
 * accessors.
 */
val NamedDomainObjectContainer<out BuildConfigSourceSet>.model
	@Suppress("UNCHECKED_CAST")
	inline get() = this as NamedDomainObjectContainer<BuildConfigSourceSet>

val NamedDomainObjectContainer<out BuildConfigSourceSet>.m
	inline get() = model
