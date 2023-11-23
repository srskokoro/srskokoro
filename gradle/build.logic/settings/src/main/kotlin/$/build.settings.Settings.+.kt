@file:Suppress("PackageDirectoryMismatch")

import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import java.io.File
import java.util.Optional


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


private const val gitRootDirOptional__name = "gitRootDirOptional"

val Settings.gitRootDir: File?
	get() = extensions.let { xs ->
		@Suppress("UNCHECKED_CAST")
		// NOTE: The cast below throws on non-null incompatible types (as intended).
		xs.findByName(gitRootDirOptional__name) as Optional<File>?
		?: run<Optional<File>> lookup@{
			var dir = settingsDir
			while (File(dir, ".git").isDirectory.not()) {
				dir = dir.parentFile ?: return@lookup Optional.empty<File>()
			}
			Optional.of(dir)
		}.also {
			xs.add(gitRootDirOptional__name, it)
		}
	}.orElse(null)

val Settings.isAtGitRoot: Boolean
	inline get() = settingsDir == gitRootDir


/**
 * Converts the given file path into a relative path string, relative to the
 * current build's settings directory (see [Settings.getSettingsDir]`()`).
 *
 * The returned value is ready to be used with [Settings.includeBuild]`()`.
 */
fun Settings.relativize(rootProject: File): String =
	rootProject.toRelativeString(base = settingsDir).ifEmpty { "." }
