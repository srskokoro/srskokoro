package build.api.dsl

import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle

/** Optimized alternative to [Gradle.gradle][Gradle.getGradle] */
@Suppress("NOTHING_TO_INLINE")
inline fun Gradle.gradle() = this

/** Optimized alternative to [Settings.settings][Settings.getSettings] */
@Suppress("NOTHING_TO_INLINE")
inline fun Settings.settings() = this

/** Optimized alternative to [Project.project][Project.getProject] */
@Suppress("NOTHING_TO_INLINE")
inline fun Project.project() = this
