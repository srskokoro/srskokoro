pluginManagement {
	includeBuild("../meta-plugins")
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

	val kotlin = "1.8.21" // https://kotlinlang.org/docs/releases.html
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

	val kotlinx_coroutines = "1.7.1"
	module("org.jetbrains.kotlinx:kotlinx-coroutines-test", kotlinx_coroutines)
	module("org.jetbrains.kotlinx:kotlinx-coroutines-core", kotlinx_coroutines)
	module("org.jetbrains.kotlinx:kotlinx-coroutines-swing", kotlinx_coroutines)

	// --

	module("org.slf4j:*", "2.0.7")

	val grgit = "5.0.0"
	plugin("org.ajoberstar.grgit", grgit)
	plugin("org.ajoberstar.grgit.service", grgit)
	module("org.ajoberstar.grgit:grgit-core", grgit)
	module("org.ajoberstar.grgit:grgit-gradle", grgit)

	val gmazzo_buildConfig = "4.0.4" // https://github.com/gmazzo/gradle-buildconfig-plugin
	plugin("com.github.gmazzo.buildconfig", gmazzo_buildConfig)
	module("com.github.gmazzo.buildconfig:plugin", gmazzo_buildConfig)

	module("org.jetbrains.kotlinx:atomicfu-gradle-plugin", "0.20.2")

	module("androidx.core:core-ktx", "1.10.1")
	module("androidx.activity:activity-ktx", "1.7.2")

	val appcompat_version = "1.6.1" // https://developer.android.com/jetpack/androidx/releases/appcompat
	module("androidx.appcompat:appcompat", appcompat_version)
	module("androidx.appcompat:appcompat-resources", appcompat_version) // For loading and tinting drawables on older versions of the platform

	module("com.squareup.okio:okio", "3.3.0")
	module("net.harawata:appdirs", "1.2.1") // https://github.com/harawata/appdirs

	module("com.github.ajalt.clikt:clikt", "3.5.2") // https://github.com/ajalt/clikt

	val flatlaf = "3.1.1"
	module("com.formdev:flatlaf", flatlaf) // https://github.com/JFormDesigner/FlatLaf
	module("com.formdev:flatlaf-extras", flatlaf) // https://github.com/JFormDesigner/FlatLaf/tree/main/flatlaf-extras
	module("com.github.Dansoftowner:jSystemThemeDetector", "3.8") // https://github.com/Dansoftowner/jSystemThemeDetector
}

dependencyBundles {
	bundle("testExtras") {
		module("org.jetbrains.kotlinx:kotlinx-coroutines-test")
		module("io.kotest:kotest-property")
	}
}
