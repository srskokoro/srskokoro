@file:Suppress("PackageDirectoryMismatch")

import org.gradle.api.Project
import org.gradle.api.provider.ProviderFactory

/**
 * `true` if the environment variable `IS_RELEASING=true` is defined; `false`
 * otherwise.
 *
 * @see Project.isReleasing
 */
val ProviderFactory.isReleasing: Boolean
	get() = @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "KotlinConstantConditions")
	("true" as java.lang.String).equalsIgnoreCase(environmentVariable("IS_RELEASING").orNull)

/**
 * `true` if the environment variable `IS_RELEASING=true` is defined; `false`
 * otherwise. Same as [`providers.isReleasing`][ProviderFactory.isReleasing]
 *
 * @see ProviderFactory.isReleasing
 * @see Project.isDebug
 */
val Project.isReleasing: Boolean
	inline get() = providers.isReleasing

/**
 * `true` if all debugging facilities should be fully enabled. The return value
 * is expected to be always the same as the following:
 * ```
 * !project.isReleasing
 * ```
 * @see Project.isReleasing
 */
val Project.isDebug: Boolean
	inline get() = !isReleasing
