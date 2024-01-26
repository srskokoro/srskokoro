import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.launcher.daemon.server.scaninfo.DaemonScanInfo

private object Build {
	var ideaInitialSyncSeen = false
}

run<Unit> {
	// Detect if we're running in a Gradle daemon.
	// - From, https://stackoverflow.com/a/55020202
	if (serviceOf<DaemonScanInfo>().isSingleUse) return@run

	if (System.getProperty("idea.sync.active") != "true") return@run
	if (Build.ideaInitialSyncSeen) return@run
	Build.ideaInitialSyncSeen = true

	// NOTE: The following will prevent the entire project from being loaded by
	// Android Studio on first load. This exists so as to prevent Android Studio
	// from analyzing Android sources as if they were for the current JDK that
	// runs Gradle (which isn't affected by Gradle's JVM toolchain support),
	// which can cause an issue in the IDE's code analysis, as it would fail for
	// when a class is referenced that's not present in Android targets but is
	// present in the current JDK (e.g., `java.lang.constant.Constable`).
	error(
		"""
		Due to issues with Android Studio's project import mechanism (that we would
		rather not deal with), a manual IDE sync has been required on first load.
		""".trimIndent()
	)
}
