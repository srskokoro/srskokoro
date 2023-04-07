@file:Suppress("PackageDirectoryMismatch")

import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import java.io.File


/**
 * Returns `true` if the current build is the main build of the composite build.
 *
 * @see Gradle.isRootBuild
 */
val Settings.isRootBuild: Boolean
	inline get() = gradle.isRootBuild


private const val rootSettingsDir__name = "rootSettingsDir"

/**
 * Returns the settings directory of the root build (see [Gradle.findRoot]`()`
 * and [Gradle.findSettingsDir]`()`).
 *
 * @see Settings.getSettingsDir
 */
val Settings.rootSettingsDir: File
	get() = extensions.let { xs ->
		// NOTE: The cast below throws on non-null incompatible types (as intended).
		xs.findByName(rootSettingsDir__name) as File?
		?: gradle.let { g ->
			if (g.isRootBuild) run<File> {
				settingsDir // Guarantees referential equality
			} else {
				g.findRoot().findSettingsDir()
			}
		}.also { dir ->
			xs.add(rootSettingsDir__name, dir)
		}
	}


/**
 * Converts the given file path into a relative path string, relative to the
 * current build's settings directory (see [Settings.getSettingsDir]`()`).
 *
 * The returned value is ready to be used with [Settings.includeBuild]`()`.
 */
fun Settings.relativize(rootProject: File): String =
	rootProject.toRelativeString(base = settingsDir).ifEmpty { "." }
