package build.support.gradle

import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.initialization.SettingsLocation
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.support.serviceOf
import java.io.File

/**
 * The *presumed* root directory of the entire composite build.
 *
 * This directory is presumably the [settings directory][org.gradle.api.initialization.Settings.getSettingsDir]
 * of the root build of the composite build.
 *
 * This directory must have a [settings file][org.gradle.initialization.SettingsLocation.getSettingsFile]
 * and a `gradle` directory containing the gradle wrapper. If no such directory
 * matching that criteria can be found, an error is thrown instead.
 *
 * Note that, this is just a "best guess" and the resolved directory may not
 * actually be the sought-after root directory. Any directory that matches the
 * said criteria may be returned.
 *
 * @throws IllegalStateException if no such directory matching the said criteria
 * can be found.
 */
val Gradle.gradleRootDir: File
	get() = extra.getOrAdd(::gradleRootDir.name) {
		run<Unit> {
			var checkDir: File = serviceOf<SettingsLocation>().settingsDir
			while (!File(checkDir, "gradle/wrapper/gradle-wrapper.jar").isFile) {
				checkDir = checkDir.parentFile ?: return@run
			}
			if (File(checkDir, "settings.gradle.kts").isFile || File(checkDir, "settings.gradle").isFile) {
				return@getOrAdd checkDir
			}
			parent?.let {
				return@getOrAdd it.gradleRootDir
			}
		}
		throw E_CannotResolveGradleRootDir()
	}

private fun E_CannotResolveGradleRootDir() = IllegalStateException(
	"Root settings directory holding a valid `gradle/wrapper` directory was not found."
)

// --

/** @see Gradle.gradleRootDir */
inline val Settings.gradleRootDir
	get() = gradle.gradleRootDir

/** @see Gradle.gradleRootDir */
inline val Project.gradleRootDir
	get() = gradle.gradleRootDir
