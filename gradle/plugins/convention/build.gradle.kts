plugins {
	`kotlin-dsl` apply false
	kotlin("jvm") // See, https://stackoverflow.com/a/72724249
}

kotlin {
	sourceSets.main {
		kotlin.srcDir(File(rootDir, "../conventions"))
	}
}

// Workaround for https://github.com/gradle/gradle/issues/21052
// - Applies `kotlin-dsl` plugin last, because it erroneously fetches source
// directories eagerly.
apply(plugin = "org.gradle.kotlin.kotlin-dsl")

dependencies {
	implementation("convention:deps-plugins")

	implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
	implementation("com.android.tools.build:gradle")
	implementation("org.jetbrains.compose:compose-gradle-plugin")

	implementation("io.kotest:kotest-framework-multiplatform-plugin-gradle")
}
