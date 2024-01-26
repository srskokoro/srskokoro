import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.launcher.daemon.server.scaninfo.DaemonScanInfo
import java.util.UUID

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

	// NOTE: The following forces Android Studio to load the entire build fully
	// on initial project sync. Otherwise, Android Studio will fail to see that
	// the build contains Android projects and simply resort to analyzing
	// Android sources as if they were for the current JDK running Gradle (that
	// isn't affected by Gradle's JVM toolchain support), which can cause issues
	// in the IDE's code analysis, as it would fail for when a referenced class
	// is not present in Android targets but is present in the current JDK
	// (e.g., `java.lang.constant.Constable`).
	// --

	val settings = settings
	val buildDir = File(settings.settingsDir, ".build")
	buildDir.mkdirs()

	val dummy = File(buildDir, "reloadOnIdeaInitialSync[dummy].settings.gradle.kts")
	UUID.randomUUID().run {
		dummy.writeText("object Build_${
			java.lang.Long.toHexString(mostSignificantBits)
		}_${
			java.lang.Long.toHexString(leastSignificantBits)
		}\n")
	}

	settings.apply(from = dummy)
	check(dummy.delete()) { "Failed to delete: $dummy" }

	println("IDE forced to reload all projects")
}
