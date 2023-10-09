plugins {
	`kotlin-dsl` apply false
	id("build.plugins.kt.dsl") apply false
	kotlin("jvm") version embeddedKotlinVersion // See, https://stackoverflow.com/a/72724249
}

kotlin {
	sourceSets.main {
		kotlin.srcDir(File(projectDir, "../conventions"))
	}
}

// Workaround for https://github.com/gradle/gradle/issues/21052
// - Applies `kotlin-dsl` plugin last, because it erroneously fetches source
// directories eagerly.
apply(plugin = "build.plugins.kt.dsl")

dependencies {
	implementation("convention:build-root")
	implementation("convention:build-support")
	implementation("convention:deps")
	implementation("convention:settings")

	// https://kotlinlang.org/docs/gradle-configure-project.html#versions-alignment-of-transitive-dependencies
	platform("org.jetbrains.kotlin:kotlin-bom").let { bom ->
		implementation(bom)
		testImplementation(bom)
	}

	implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
	implementation("com.android.tools.build:gradle")

	implementation("io.kotest:kotest-framework-api") // So that we get access to, e.g., `io.kotest.core.internal.KotestEngineProperties`
	implementation("io.kotest:kotest-framework-multiplatform-plugin-gradle")

	implementation("org.ajoberstar.grgit:grgit-gradle")
	implementation("com.github.gmazzo.buildconfig:plugin")
	implementation("org.jetbrains.kotlinx:atomicfu-gradle-plugin")

	implementation("app.cash.redwood:redwood-gradle-plugin")
}
