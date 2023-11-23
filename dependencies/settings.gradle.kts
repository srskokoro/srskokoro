pluginManagement {
	includeBuild("../gradle/build.logic")
}
plugins {
	id("build.settings.deps")
}

dependencyVersionsSetup {
	export()
}

dependencyVersions {
	jvm {
		ver = 17
		vendor { ADOPTIUM }
	}

	val kotlin = "1.9.20" // https://kotlinlang.org/docs/releases.html
	plugin("org.jetbrains.kotlin.*", kotlin)
	module("org.jetbrains.kotlin:*", kotlin)
	plugin("org.jetbrains.kotlin.plugin.serialization", kotlin)
	val kotlinx_serialization = "1.6.0" // https://github.com/Kotlin/kotlinx.serialization/releases
	module("org.jetbrains.kotlinx:kotlinx-serialization-json", kotlinx_serialization)
	module("org.jetbrains.kotlinx:kotlinx-serialization-json-okio", kotlinx_serialization)
	module("org.jetbrains.kotlinx:kotlinx-serialization-cbor", kotlinx_serialization)

	// https://github.com/LouisCAD/CompleteKotlin/releases
	plugin("com.louiscad.complete-kotlin", "1.1.0")

	val android = "8.1.2" // Android Gradle Plugin (AGP)
	plugin("com.android.*", android)
	module("com.android.tools.build:gradle", android)

	val kotest = "5.6.2" // https://github.com/kotest/kotest/releases
	plugin("io.kotest.multiplatform", kotest)
	module("io.kotest:kotest-framework-multiplatform-plugin-gradle", kotest)
	module("io.kotest:kotest-runner-junit5", kotest)
	module("io.kotest:kotest-framework-api", kotest)
	module("io.kotest:kotest-framework-engine", kotest)
	module("io.kotest:kotest-assertions-core", kotest)
	module("io.kotest:kotest-property", kotest)

	val kotlinx_coroutines = "1.7.3" // https://github.com/Kotlin/kotlinx.coroutines/releases
	module("org.jetbrains.kotlinx:kotlinx-coroutines-test", kotlinx_coroutines)
	module("org.jetbrains.kotlinx:kotlinx-coroutines-core", kotlinx_coroutines)
	module("org.jetbrains.kotlinx:kotlinx-coroutines-swing", kotlinx_coroutines)

	val atomicfu = "0.22.0" // https://github.com/Kotlin/kotlinx-atomicfu/releases
	module("org.jetbrains.kotlinx:atomicfu-gradle-plugin", atomicfu)
	module("org.jetbrains.kotlinx:atomicfu", atomicfu)

	val androidx_annotation = "1.7.0"// https://developer.android.com/jetpack/androidx/releases/annotation
	module("androidx.annotation:annotation", androidx_annotation)

	// --

	val slf4j = "2.0.7"
	module("org.slf4j:*", slf4j)

	val grgit = "5.0.0"
	plugin("org.ajoberstar.grgit", grgit)
	plugin("org.ajoberstar.grgit.service", grgit)
	module("org.ajoberstar.grgit:grgit-core", grgit)
	module("org.ajoberstar.grgit:grgit-gradle", grgit)

	val gmazzo_buildConfig = "4.1.2" // https://github.com/gmazzo/gradle-buildconfig-plugin
	plugin("com.github.gmazzo.buildconfig", gmazzo_buildConfig)
	module("com.github.gmazzo.buildconfig:plugin", gmazzo_buildConfig)

	val closure_compiler = "v20230502" // https://github.com/google/closure-compiler/tags
	// - See also, https://github.com/gradle-webtools/gradle-minify-plugin
	module("com.google.javascript:closure-compiler", closure_compiler)

	// --

	module("androidx.core:core-ktx", "1.12.0")
	module("androidx.activity:activity-ktx", "1.8.0")

	val appcompat = "1.6.1" // https://developer.android.com/jetpack/androidx/releases/appcompat
	module("androidx.appcompat:appcompat", appcompat)
	module("androidx.appcompat:appcompat-resources", appcompat) // For loading and tinting drawables on older versions of the platform

	val androidx_webkit = "1.8.0" // https://developer.android.com/jetpack/androidx/releases/webkit
	module("androidx.webkit:webkit", androidx_webkit)

	// --

	val korge = "4.0.10" // https://github.com/korlibs/korge/releases
	module("com.soywiz.korlibs.kds:kds", korge)

	val okio = "3.6.0" // https://square.github.io/okio/changelog/
	module("com.squareup.okio:okio", okio)

	module("net.harawata:appdirs", "1.2.1") // https://github.com/harawata/appdirs
	module("com.github.ajalt.clikt:clikt", "4.2.0") // https://github.com/ajalt/clikt/releases

	// --

	val flatlaf = "3.1.1"
	module("com.formdev:flatlaf", flatlaf) // https://github.com/JFormDesigner/FlatLaf
	module("com.formdev:flatlaf-extras", flatlaf) // https://github.com/JFormDesigner/FlatLaf/tree/main/flatlaf-extras
	module("com.github.Dansoftowner:jSystemThemeDetector", "3.8") // https://github.com/Dansoftowner/jSystemThemeDetector

	val redwood = "0.7.0" // https://github.com/cashapp/redwood/releases
	// NOTE: Must be kept consistent with Redwood. See,
	// https://github.com/cashapp/redwood/blob/0.7.0/gradle/libs.versions.toml#L6
	val jb_compose = "1.5.1"
	// The compose compiler plugin to use for Redwood -- https://github.com/cashapp/redwood/tree/0.7.0#custom-compose-compiler
	// - See also, https://github.com/JetBrains/compose-multiplatform/blob/v1.5.10/gradle-plugins/compose/src/main/kotlin/org/jetbrains/compose/ComposeCompilerCompatibility.kt
	val jb_compose_compiler = "1.5.3"
	module("app.cash.redwood:*", redwood)
	plugin("app.cash.redwood", redwood)
	plugin("app.cash.redwood.*", redwood)
	plugin("app.cash.redwood.generator.*", redwood)
	module("org.jetbrains.compose.runtime:*", jb_compose)
	module("org.jetbrains.compose.compiler:compiler", jb_compose_compiler)

	val voyager = "1.0.0-rc07" // https://github.com/adrielcafe/voyager/releases
	module("cafe.adriel.voyager:*", voyager)
}
