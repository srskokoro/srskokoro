@file:Suppress("NOTHING_TO_INLINE")

package build.api.dsl

import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle

/** Optimized alternative to [Gradle.gradle][Gradle.getGradle] */
inline fun Gradle.gradleThis() = this

/**
 * Optimized alternative to [Gradle.gradle][Gradle.getGradle]
 * @see gradleThis
 */
inline val Gradle.gradleThis get() = this


/** Optimized alternative to [Settings.settings][Settings.getSettings] */
inline fun Settings.settingsThis() = this

/**
 * Optimized alternative to [Settings.settings][Settings.getSettings]
 * @see settingsThis
 */
inline val Settings.settingsThis get() = this


/** Optimized alternative to [Project.project][Project.getProject] */
inline fun Project.projectThis() = this

/**
 * Optimized alternative to [Project.project][Project.getProject]
 * @see projectThis
 */
inline val Project.projectThis get() = this
