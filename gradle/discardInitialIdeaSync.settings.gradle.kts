import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.launcher.daemon.server.scaninfo.DaemonScanInfo

private object Build {
	// Will be preserved by the Gradle daemon
	var isInitialIdeaSyncSeen = false
}

run<Unit>(fun() {
	// See, https://github.com/JetBrains/kotlin/blob/v1.7.22/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/internal/idea.kt
	if (System.getProperty("idea.sync.active") != "true") return // Not in IDEA sync

	// Detect if we're running in a Gradle daemon.
	// - From, https://stackoverflow.com/a/55020202
	if (serviceOf<DaemonScanInfo>().isSingleUse) return // Not running in daemon

	if (Build.isInitialIdeaSyncSeen) return
	Build.isInitialIdeaSyncSeen = true

	// NOTE: The following prevents Android Studio from loading projects on
	// initial project sync. This exists so as to prevent Android Studio from
	// analyzing source code right away, since it also has the consequence of
	// treating Android source code as if they were for the current JDK running
	// Gradle (that isn't affected by Gradle's JVM toolchain support), which can
	// cause issues in the IDE's code analysis, as it would fail for when a
	// referenced class is not present in Android targets but is present in the
	// current JDK (e.g., `java.lang.constant.Constable`).
	// --

	throw Error(
		"""
		Due to issues with Android Studio's project loading (that we would rather not
		deal with), projects were not loaded yet: a manual IDE sync must be done.
		""".trimIndent()
	)
})
