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

	val android = "7.4.0" // Android Gradle Plugin (AGP)
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
}

dependencyBundles {
	bundle("testExtras") {
		module("org.jetbrains.kotlinx:kotlinx-coroutines-test")
		module("io.kotest:kotest-property")
	}
}
