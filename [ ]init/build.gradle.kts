import build.api.dsl.*

plugins {
	id("build.kt.android.lib")

	// Apply Android plugin directly to let Android Studio see that this project
	// is targeting Android, so as to prevent the IDE's code analysis from
	// treating the source code as if they were for the current JDK that runs
	// Gradle (which isn't affected by Gradle's JVM toolchain support), which
	// can cause an issue in the IDE's code analysis, as it would fail for when
	// a class is referenced that's not present in Android targets but is
	// present in the current JDK (e.g., `java.lang.constant.Constable`).
	id("com.android.library")
}

android {
	namespace = "main"
}

// Hopefully, the following will ensure that this project gets seen consistently
// by the IDE on Gradle project sync.
tasks.ideSyncTask
