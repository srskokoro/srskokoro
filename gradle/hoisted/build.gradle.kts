plugins {
	id("build.root")
}

// The following is expected to be a valid Android-only project, which we
// evaluate first in order for Android Studio to see that the build contains
// Android sources. Without this, Android Studio will analyze sources for
// Android targets as if they were for the current JDK, which can cause an issue
// in the IDE's code analysis, as it would fail for when a class is referenced
// that's not present in Android targets but is present in the current JDK
// (e.g., `java.lang.constant.Constable`).
//
// Note that, the "current JDK" said above is the "Gradle JDK" used by Android
// Studio for running Gradle and syncing projects. It's not the same as the JDK
// set up via Gradle's JVM toolchain support. (At least, that's how things work
// at the time of writing.)
evaluationDependsOn(":init")
