@file:Suppress("PackageDirectoryMismatch")

import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.initialization.SettingsLocation
import org.gradle.kotlin.dsl.support.serviceOf
import java.io.File

/**
 * Finds the settings directory of the build. The settings directory is the
 * directory containing the settings file.
 *
 * @see Settings.getSettingsDir
 * @see Settings.rootSettingsDir
 */
fun Gradle.findSettingsDir(): File = serviceOf<SettingsLocation>().settingsDir

/**
 * Returns `true` if this build is the main build of the composite build.
 *
 * @see Settings.isRootBuild
 * @see Gradle.findRoot
 */
val Gradle.isRootBuild: Boolean
	inline get() = parent == null

/**
 * Finds the root build of this build, or this build itself if is has no parent
 * (i.e., if it's already the root build).
 *
 * @return The root build or this build if it's the root build already.
 *
 * @see Gradle.isRootBuild
 * @see Gradle.getParent
 */
fun Gradle.findRoot(): Gradle {
	var gradle = this
	var parent = gradle.parent

	while (parent != null) {
		gradle = parent
		parent = gradle.parent
	}

	return gradle
}