@file:Suppress("UnstableApiUsage")

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

	val kotlin = "1.8.10"
	"org.jetbrains.kotlin".let {
		plugin("$it.*", kotlin)
		module("$it:*", kotlin)
	}

	val android = "7.4.2" // Android Gradle Plugin (AGP)
	plugin("com.android.*", android)
	module("com.android.tools.build:gradle", android)

	val kotest = "5.5.4"
	plugin("io.kotest.multiplatform", kotest)
	module("io.kotest:kotest-framework-multiplatform-plugin-gradle", kotest)
	module("io.kotest:kotest-runner-junit5", kotest)
	module("io.kotest:kotest-framework-engine", kotest)
	module("io.kotest:kotest-assertions-core", kotest)
	module("io.kotest:kotest-property", kotest)

	val kotlinx_coroutines = "1.6.4"
	module("org.jetbrains.kotlinx:kotlinx-coroutines-test", kotlinx_coroutines)
	module("org.jetbrains.kotlinx:kotlinx-coroutines-core", kotlinx_coroutines)
	module("org.jetbrains.kotlinx:kotlinx-coroutines-swing", kotlinx_coroutines)

	// --

	val grgit = "5.0.0"
	plugin("org.ajoberstar.grgit", grgit)
	plugin("org.ajoberstar.grgit.service", grgit)
	module("org.ajoberstar.grgit:grgit-core", grgit)
	module("org.ajoberstar.grgit:grgit-gradle", grgit)

	val gmazzo_buildConfig = "3.1.0" // https://github.com/gmazzo/gradle-buildconfig-plugin
	plugin("com.github.gmazzo.buildconfig", gmazzo_buildConfig)
	module("com.github.gmazzo:gradle-buildconfig-plugin", gmazzo_buildConfig)

	module("org.jetbrains.kotlinx:atomicfu-gradle-plugin", "0.20.2")

	module("androidx.core:core-ktx", "1.10.0")
	module("androidx.activity:activity-ktx", "1.7.0")

	module("com.squareup.okio:okio", "3.3.0")
	module("net.harawata:appdirs", "1.2.1") // https://github.com/harawata/appdirs

	module("com.github.ajalt.clikt:clikt", "3.5.2") // https://github.com/ajalt/clikt

	module("com.formdev:flatlaf", "3.1.1") // https://github.com/JFormDesigner/FlatLaf
}

dependencyBundles {
	bundle("testExtras") {
		module("org.jetbrains.kotlinx:kotlinx-coroutines-test")
		module("io.kotest:kotest-property")
	}
}
