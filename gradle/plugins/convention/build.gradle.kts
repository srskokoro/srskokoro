plugins {
	id("conv.kt.dsl") apply false
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
apply(plugin = "conv.kt.dsl")

dependencies {
	implementation("convention:build-support")
	implementation("convention:deps")

	// https://kotlinlang.org/docs/gradle-configure-project.html#versions-alignment-of-transitive-dependencies
	implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

	implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
	implementation("com.android.tools.build:gradle")

	implementation("io.kotest:kotest-framework-api") // So that we get access to, e.g., `io.kotest.core.internal.KotestEngineProperties`
	implementation("io.kotest:kotest-framework-multiplatform-plugin-gradle")

	implementation("org.ajoberstar.grgit:grgit-gradle")
	implementation("com.github.gmazzo.buildconfig:plugin")
	implementation("org.jetbrains.kotlinx:atomicfu-gradle-plugin")

	implementation("app.cash.redwood:redwood-gradle-plugin")
}
