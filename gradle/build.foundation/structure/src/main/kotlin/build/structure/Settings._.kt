package build.structure

import build.api.dsl.*
import build.support.getFileUri
import build.support.io.safeResolve
import org.gradle.api.initialization.Settings
import org.gradle.initialization.SettingsLocation
import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.launcher.daemon.server.scaninfo.DaemonScanInfo
import java.io.File

private fun Settings.getStructureRoot(): File {
	val structureRootValue = extra.getOrElse<Any>("build.structure.root") {
		error("Must set up structure root path via extra property key \"$it\" (or via 'gradle.properties' file)")
	}
	val structureRoot = rootDir.safeResolve(structureRootValue.toString()).canonicalFile
	// Guard against an unintended path set as the structure root
	if (!File(structureRoot, "settings.gradle.kts").exists() && !File(structureRoot, "settings.gradle").exists()) {
		error("Structure root must be a valid build with a Gradle `settings` file (even if empty)" +
			"\n- Structure root: $structureRoot")
	}
	return structureRoot
}

internal fun Settings.include(structure: ProjectStructure) {
	if (shouldDisableDueToInitialIdeaSync()) {
		// Also disable the root project's build file, in case it references
		// subprojects in the structure.
		rootProject.run { buildFileName = "!${buildFileName}" }
		return // Skip code below
	}

	val structureRoot = getStructureRoot()
	structure.findProjects(structureRoot, providers).forEach {
		val id = it.getProjectId(structureRoot)
		include(id) // NOTE: Resolves relative to `Settings.rootDir`
		project(id).projectDir = it.getProjectDir(structureRoot)
	}
}

private const val isIdeaInitialSync__name = "--build.structure.isIdeaInitialSync--"

// Will be preserved by the Gradle daemon
private var isIdeaInitialSyncSeen = false

private const val E_DISABLED_DUE_TO_INITIAL_IDEA_SYNC = "" +
	"Due to issues with Android Studio's project loading (that we would rather not\n" +
	"deal with), projects were not loaded yet: a manual IDE sync must be done.\n"

private fun Settings.shouldDisableDueToInitialIdeaSync(): Boolean {
	// NOTE: The following prevents Android Studio from loading projects on
	// initial project sync. This exists so as to prevent Android Studio from
	// analyzing source code right away, since it also has the consequence of
	// treating Android source code as if they were for the current JDK running
	// Gradle (that isn't affected by Gradle's JVM toolchain support), which can
	// cause issues in the IDE's code analysis, as it would fail for when a
	// referenced class is not present in Android targets but is present in the
	// current JDK (e.g., `java.lang.constant.Constable`).
	// --

	val settingsExtra = extra
	if (settingsExtra.parseBoolean("build.structure.disableOnIdeaInitialSync")) {
		val isIdeaInitialSync = settingsExtra.getOrAdd(isIdeaInitialSync__name) {
			val rootGradle = gradle.findRoot()
			rootGradle.extra.getOrAdd(isIdeaInitialSync__name) init@{
				run<Unit> {
					// Detect if we're running in a Gradle daemon.
					// - From, https://stackoverflow.com/a/55020202
					if (rootGradle.serviceOf<DaemonScanInfo>().isSingleUse) return@run // Not running in daemon

					// See, https://github.com/JetBrains/kotlin/blob/v1.7.22/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/internal/idea.kt
					if (System.getProperty("idea.sync.active") != "true") return@run // Not in IDEA sync

					if (isIdeaInitialSyncSeen) return@run
					isIdeaInitialSyncSeen = true

					System.err.println(buildString {
						append("w: ")
						rootGradle.serviceOf<SettingsLocation>().settingsFile?.let {
							append(getFileUri(it))
							append(":0\n")
						}
						append(E_DISABLED_DUE_TO_INITIAL_IDEA_SYNC)
					})
					return@init true
				}
				false
			}
		}
		if (isIdeaInitialSync) return true
	}
	return false
}
