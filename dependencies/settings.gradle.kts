pluginManagement {
	includeBuild("../gradle/build-logic")
}
plugins {
	id("conv.settings.deps")
}

dependencyVersionsSetup {
	export()
}

dependencyVersions {
	jvm {
		ver = 17
		vendor { ADOPTIUM }
	}

	val kotlin = "1.9.10" // https://kotlinlang.org/docs/releases.html
	"org.jetbrains.kotlin".let {
		plugin("$it.*", kotlin)
		module("$it:*", kotlin)
	}

	plugin("com.louiscad.complete-kotlin", "1.1.0") // https://github.com/LouisCAD/CompleteKotlin

	val android = "8.0.2" // Android Gradle Plugin (AGP)
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

	val appcompat_version = "1.6.1" // https://developer.android.com/jetpack/androidx/releases/appcompat
	module("androidx.appcompat:appcompat", appcompat_version)
	module("androidx.appcompat:appcompat-resources", appcompat_version) // For loading and tinting drawables on older versions of the platform

	// --

	val korge = "4.0.9" // https://github.com/korlibs/korge/releases
	module("com.soywiz.korlibs.kds:kds", korge)

	module("com.squareup.okio:okio", "3.3.0")

	module("net.harawata:appdirs", "1.2.1") // https://github.com/harawata/appdirs
	module("com.github.ajalt.clikt:clikt", "4.2.0") // https://github.com/ajalt/clikt/releases

	// --

	val flatlaf = "3.1.1"
	module("com.formdev:flatlaf", flatlaf) // https://github.com/JFormDesigner/FlatLaf
	module("com.formdev:flatlaf-extras", flatlaf) // https://github.com/JFormDesigner/FlatLaf/tree/main/flatlaf-extras
	module("com.github.Dansoftowner:jSystemThemeDetector", "3.8") // https://github.com/Dansoftowner/jSystemThemeDetector

	val redwood = "0.7.0" // https://github.com/cashapp/redwood/releases
	module("app.cash.redwood:*", redwood)
	plugin("app.cash.redwood", redwood)
	plugin("app.cash.redwood.*", redwood)
	plugin("app.cash.redwood.generator.*", redwood)

	val voyager = "1.0.0-rc06" // https://github.com/adrielcafe/voyager/releases
	module("cafe.adriel.voyager:*", voyager)
}

dependencyBundles {
	bundle("testExtras") {
		module("org.jetbrains.kotlinx:kotlinx-coroutines-test")
		module("io.kotest:kotest-property")
	}
}
