// TODO: Remove once fixed, https://github.com/gradle/gradle/issues/22797
@file:Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage")

plugins {
	// This is necessary to avoid the plugins to be loaded multiple times in
	// each subproject's classloader
	kotlin("jvm") version libs.versions.kotlin apply false
	kotlin("multiplatform") version libs.versions.kotlin apply false
	kotlin("android") version libs.versions.kotlin apply false
	id("com.android.application") version libs.versions.android apply false
	id("com.android.library") version libs.versions.android apply false
	id("org.jetbrains.compose") version libs.versions.compose.mpp apply false
	id("io.kotest.multiplatform") version libs.versions.kotest apply false

	// Prefer always last
	id("convention") apply false
}

allprojects {
	group = "srs.kokoro"
}
