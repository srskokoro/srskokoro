package build.api.dsl

import org.gradle.api.Project
import org.gradle.api.provider.ProviderFactory

/**
 * `true` if the system property `IS_RELEASING=true` is defined; `false`
 * otherwise.
 *
 * @see Project.isReleasing
 * @see ProviderFactory.isDebug
 */
val ProviderFactory.isReleasing: Boolean
	get() = @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
	("true" as java.lang.String).equalsIgnoreCase(systemProperty("IS_RELEASING").orNull)

/**
 * `true` if all debugging facilities should be fully enabled.
 *
 * **Implementation:**
 *
 * `false` if the system property `IS_RELEASING=true` is defined; `true`
 * otherwise. Equivalent to the following:
 * ```
 * !this.isReleasing
 * ```
 *
 * @see Project.isDebug
 * @see ProviderFactory.isReleasing
 */
val ProviderFactory.isDebug: Boolean
	inline get() = !isReleasing

/**
 * `true` if the system property `IS_RELEASING=true` is defined; `false`
 * otherwise. Same as [`providers.isReleasing`][ProviderFactory.isReleasing]
 *
 * @see ProviderFactory.isReleasing
 * @see Project.isDebug
 */
val Project.isReleasing: Boolean
	inline get() = providers.isReleasing

/**
 * `true` if all debugging facilities should be fully enabled. Same as
 * [`providers.isDebug`][ProviderFactory.isDebug]
 *
 * **Implementation:**
 *
 * `false` if the system property `IS_RELEASING=true` is defined; `true`
 * otherwise. Equivalent to the following:
 * ```
 * !this.isReleasing
 * ```
 *
 * @see ProviderFactory.isDebug
 * @see Project.isReleasing
 */
val Project.isDebug: Boolean
	inline get() = providers.isDebug
