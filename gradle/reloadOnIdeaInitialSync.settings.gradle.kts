import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.launcher.daemon.server.scaninfo.DaemonScanInfo

private object Build {
	var ideaInitialSyncSeen = false
}

run<Unit> {
	// Detect if we're running in a Gradle daemon.
	// - From, https://stackoverflow.com/a/55020202
	if (serviceOf<DaemonScanInfo>().isSingleUse) return@run

	// See, https://github.com/JetBrains/kotlin/blob/v1.7.22/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/internal/idea.kt
	if (System.getProperty("idea.sync.active") != "true") return@run

	if (Build.ideaInitialSyncSeen) return@run
	Build.ideaInitialSyncSeen = true

	// NOTE: The following forces Android Studio to load the entire build fully
	// on initial project sync. This exists so as to prevent Android Studio from
	// analyzing source code right away, since it also has the consequence of
	// treating Android source code as if they were for the current JDK running
	// Gradle (that isn't affected by Gradle's JVM toolchain support), which can
	// cause issues in the IDE's code analysis, as it would fail for when a
	// referenced class is not present in Android targets but is present in the
	// current JDK (e.g., `java.lang.constant.Constable`).
	// --

	val settings = settings
	val workDir = File(settings.settingsDir, ".build/reloadOnIdeaInitialSync")
	workDir.deleteRecursively()
	workDir.mkdirs()

	val dummy = java.util.UUID.randomUUID().run {
		File(workDir, "dummy_${
			java.lang.Long.toHexString(mostSignificantBits)
		}_${
			java.lang.Long.toHexString(leastSignificantBits)
		}.settings.gradle.kts")
	}
	dummy.writeText("println(\"IDE forced to reload all projects\")\n")

	settings.apply(from = dummy)
}
