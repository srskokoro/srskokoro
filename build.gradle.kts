// TODO: Remove once fixed, https://github.com/gradle/gradle/issues/22797
@file:Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage")

plugins {
	id("convention.root")

	// Avoid the plugins to be loaded multiple times in each subproject's
	// classloader. See also, https://youtrack.jetbrains.com/issue/KT-46200
	kotlin("jvm") apply false
	kotlin("multiplatform") apply false
	kotlin("android") apply false
	id("com.android.application") apply false
	id("com.android.library") apply false
	id("org.jetbrains.compose") apply false
	id("io.kotest.multiplatform") apply false
}

allprojects {
	group = "srs.kokoro"
}
