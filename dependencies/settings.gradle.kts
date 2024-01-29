pluginManagement {
	includeBuild("../gradle/build.foundation")
}
plugins {
	id("build.dependencies")
}

dependencySettings {
	exportOnly()

	// -=*=-
	// Build foundations

	/** Should correspond to [JavaToolchainSpec] */
	run<Unit> {
		prop("jvm.lang", 21)
		prop("jvm.vendor", JvmVendorSpec::ADOPTIUM.name)
		prop("jvm.implementation", JvmImplementation::VENDOR_SPECIFIC.name)
	}

	val kotlin = "1.9.21" // https://kotlinlang.org/docs/releases.html

	plugin(kotlin("android"), kotlin)
	plugin(kotlin("jvm"), kotlin)
	plugin(kotlin("multiplatform"), kotlin)

	// See also, https://youtrack.jetbrains.com/issue/KT-54691#focus=Comments-27-6852272.0-0
	module(kotlin("gradle-plugins-bom"), kotlin)
	module(kotlin("gradle-plugin"), kotlin)

	plugin(kotlin("plugin.assignment"), kotlin)
	module(kotlin("assignment"), kotlin)
	plugin(kotlin("plugin.sam.with.receiver"), kotlin)
	module(kotlin("sam-with-receiver"), kotlin)

	// See, https://kotlinlang.org/docs/gradle-configure-project.html#versions-alignment-of-transitive-dependencies
	module(kotlin("bom"), kotlin)
	module(kotlin("stdlib"), kotlin)
	module(kotlin("reflect"), kotlin)
	module(kotlin("test"), kotlin)
	module(kotlin("test-junit5"), kotlin)

	// Android Gradle Plugin (AGP) -- https://developer.android.com/build/releases/gradle-plugin
	val android = "8.2.2" // https://maven.google.com/web/index.html#com.android.tools.build:gradle

	plugin("com.android.library", android)
	plugin("com.android.application", android)
	module("com.android.tools.build:gradle", android)

	// The Android SDK Build Tools to use -- https://developer.android.com/tools/releases/build-tools
	// - See also, https://developer.android.com/build/releases/gradle-plugin#compatibility
	prop("build.android.buildToolsVersion", "34.0.0")

	prop("build.android.compileSdk", 34)
	prop("build.android.targetSdk", 34)

	// NOTE: API level 26 is the lowest SDK that we know of that fully supports
	// all Java 8 features and APIs. See also,
	// - https://developer.android.com/studio/write/java8-support#library-desugaring
	// - https://developer.android.com/studio/write/java11-default-support-table
	// - https://developer.android.com/build/jdks#compileSdk
	// - https://stackoverflow.com/q/54129834
	//
	// See also, OpenJDK support per Android version:
	// - OpenJDK 17: https://developer.android.com/about/versions/14/features#core-libraries
	// - OpenJDK 11: https://developer.android.com/about/versions/13/features#core-libraries
	// - OpenJDK 8: https://developer.android.com/about/versions/oreo/android-8.0#rt
	//
	prop("build.android.minSdk", 26)

	// The target OpenJDK for Android projects, which may be supported via
	// desugaring. See also, https://developer.android.com/studio/write/java8-support#library-desugaring
	prop("build.android.openjdk", 17)

	// Java 8+ API desugaring support -- https://developer.android.com/studio/write/java8-support#library-desugaring
	// - See also, https://developer.android.com/studio/write/java11-default-support-table
	val android_desugar = "2.0.4" // https://maven.google.com/web/index.html#com.android.tools:desugar_jdk_libs
	module("com.android.tools:desugar_jdk_libs_minimal", android_desugar)
	module("com.android.tools:desugar_jdk_libs", android_desugar)
	module("com.android.tools:desugar_jdk_libs_nio", android_desugar)

	// -=*=-
	// Build support

	val gmazzo_buildconfig = "5.3.5" // https://github.com/gmazzo/gradle-buildconfig-plugin/releases
	plugin("com.github.gmazzo.buildconfig", gmazzo_buildconfig)
	module("com.github.gmazzo.buildconfig".plugin(), gmazzo_buildconfig)

	// -=*=-
	// Test infrastructure

	val kotest = "5.8.0" // https://github.com/kotest/kotest/releases
	plugin("io.kotest.multiplatform", kotest)
	module("io.kotest.multiplatform".plugin(), kotest)
	module("io.kotest:kotest-assertions-shared", kotest)
	module("io.kotest:kotest-framework-engine", kotest)
	module("io.kotest:kotest-runner-junit5", kotest)
	module("io.kotest:kotest-property", kotest)

	val assertk = "0.28.0" // https://github.com/willowtreeapps/assertk/releases
	module("com.willowtreeapps.assertk:assertk", assertk)
}
