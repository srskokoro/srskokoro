package org.gradle.kotlin.dsl

import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

/**
 * Necessary because (at the moment) Gradle's Kotlin DSL doesn't yet respect
 * "use-site variance / type projections" when generating type-safe model
 * accessors.
 *
 * @see NamedDomainObjectContainer.m
 * @see BuildConfigExtension.ss
 */
val NamedDomainObjectContainer<out BuildConfigSourceSet>.model
	@Suppress("UNCHECKED_CAST")
	inline get() = this as NamedDomainObjectContainer<BuildConfigSourceSet>

/**
 * @see NamedDomainObjectContainer.model
 * @see BuildConfigExtension.ss
 */
val NamedDomainObjectContainer<out BuildConfigSourceSet>.m
	inline get() = model

// --

/**
 * @see NamedDomainObjectContainer.model
 * @see NamedDomainObjectContainer.m
 */
val BuildConfigExtension.ss inline get() = sourceSets.model

/**
 * @see NamedDomainObjectContainer.model
 * @see NamedDomainObjectContainer.m
 */
fun BuildConfigExtension.ss(configure: Action<NamedDomainObjectContainer<BuildConfigSourceSet>>) = configure.execute(ss)
