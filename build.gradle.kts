plugins {
	id("conv.root")

	// Avoid the plugins to be loaded multiple times in each subproject's
	// classloader. See also, https://youtrack.jetbrains.com/issue/KT-46200
	kotlin("multiplatform") apply false
	kotlin("android") apply false
	kotlin("jvm") apply false
	kotlin("js") apply false
	id("com.android.application") apply false
	id("com.android.library") apply false
	id("io.kotest.multiplatform") apply false

	id("com.louiscad.complete-kotlin")
}

// NOTE: Only modify the `group` for direct subprojects of this project; let
// Gradle automatically provide a unique `group` for subprojects of subprojects.
// - See also, https://github.com/gradle/gradle/issues/847#issuecomment-1205001575
childProjects.values.forEach {
	it.group = "srs.kokoro"
}
