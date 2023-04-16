@file:Suppress("PackageDirectoryMismatch")

import org.gradle.api.Project
import org.gradle.api.provider.ProviderFactory

/**
 * `true` if the environment variable `IS_RELEASING=true` is defined; `false`
 * otherwise.
 */
val ProviderFactory.isReleasing: Boolean
	get() = @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "KotlinConstantConditions")
	("true" as java.lang.String).equalsIgnoreCase(environmentVariable("IS_RELEASING").orNull)

/**
 * `true` if the environment variable `IS_RELEASING=true` is defined; `false`
 * otherwise. Same as [`providers.isReleasing`][ProviderFactory.isReleasing]
 *
 * @see ProviderFactory.isReleasing
 */
val Project.isReleasing: Boolean
	inline get() = providers.isReleasing
