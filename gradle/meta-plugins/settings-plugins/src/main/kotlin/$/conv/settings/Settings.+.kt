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
 * Returns the settings directory (see [Gradle.settingsDir]) of the root build
 * (see [Gradle.findRoot]`()`).
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
				g.findRoot().settingsDir
			}
		}.also { dir ->
			xs.add(rootSettingsDir__name, dir)
		}
	}


private const val rootSettingsDirRel__name = "rootSettingsDirRel"

/**
 * Returns the settings directory (see [Gradle.settingsDir]) of the root build
 * (see [Gradle.findRoot]`()`) as a string relative to the current build's
 * settings directory.
 *
 * @see Settings.rootSettingsDir
 * @see Settings.getSettingsDir
 */
val Settings.rootSettingsDirRel: String
	get() = extensions.let { xs ->
		// NOTE: The cast below throws on non-null incompatible types (as intended).
		xs.findByName(rootSettingsDirRel__name) as String?
		?: rootSettingsDir.toRelativeString(base = settingsDir).also {
			xs.add(rootSettingsDirRel__name, it)
		}
	}


/**
 * Given a directory path relative to the root build's settings directory (see
 * [Settings.rootSettingsDir]), determines if it has a gradle settings file, and
 * if so, returns the path as instead relative to the current build's settings
 * directory. Otherwise, returns null.
 *
 * The returned value is ready to be used with [Settings.includeBuild]`()`.
 *
 * @see Settings.getSettingsDir
 */
internal fun Settings.locateForIncludeBuild(pathFromRootBuild: String): String? {
	val rootProject = File(rootSettingsDir, pathFromRootBuild)
	return if (
		File(rootProject, "settings.gradle.kts").exists() ||
		File(rootProject, "settings.gradle").exists()
	) {
		File(rootSettingsDirRel, pathFromRootBuild).path
	} else null
}
