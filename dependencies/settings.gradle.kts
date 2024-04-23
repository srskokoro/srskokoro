pluginManagement {
	includeBuild("../gradle/build.foundation")
}
plugins {
	id("build.dependencies")
}

dependencySettings {
	exportOnly()

	//#region Build foundations

	/** Should correspond to [JavaToolchainSpec] */
	run<Unit> {
		prop("jvm.lang", 21)
		prop("jvm.vendor", JvmVendorSpec::ADOPTIUM.name)
		prop("jvm.implementation", JvmImplementation::VENDOR_SPECIFIC.name)
	}

	val kotlin = "1.9.23" // https://kotlinlang.org/docs/releases.html

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

	// https://github.com/LouisCAD/CompleteKotlin/releases
	plugin("com.louiscad.complete-kotlin", "1.1.0")

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

	//#endregion

	//#region Build support

	// WiX Toolset v3 -- https://github.com/wixtoolset/wix3/releases/
	val wixBinZipUrl = "https://github.com/wixtoolset/wix3/releases/download/wix3141rtm/wix314-binaries.zip"
	prop("build.wixBinZipUrl", wixBinZipUrl)

	val download = "5.6.0" // https://github.com/michel-kraemer/gradle-download-task/releases
	plugin("de.undercouch.download", download)
	module("de.undercouch.download".marker(), download)

	val shadow = "8.1.1" // https://github.com/johnrengelman/shadow/releases
	plugin("com.github.johnrengelman.shadow", shadow)
	module("com.github.johnrengelman.shadow".marker(), shadow)

	val gmazzo_buildconfig = "5.3.5" // https://github.com/gmazzo/gradle-buildconfig-plugin/releases
	plugin("com.github.gmazzo.buildconfig", gmazzo_buildconfig)
	module("com.github.gmazzo.buildconfig".marker(), gmazzo_buildconfig)

	//#endregion

	//#region General support

	val slf4j = "2.0.12" // https://www.slf4j.org/download.html
	module("org.slf4j:slf4j-jdk14", slf4j) // Needed for `jSystemThemeDetector`

	val androidx_annotation = "1.7.1"// https://developer.android.com/jetpack/androidx/releases/annotation
	module("androidx.annotation:annotation", androidx_annotation)

	val jna = "5.14.0" // https://github.com/java-native-access/jna/blob/master/CHANGES.md
	module("net.java.dev.jna:jna", jna)
	module("net.java.dev.jna:jna-platform", jna)

	//#endregion

	//#region Concurrency support

	val kotlinx_atomicfu = "0.23.2" // https://github.com/Kotlin/kotlinx-atomicfu/releases
	module("org.jetbrains.kotlinx:atomicfu-gradle-plugin", kotlinx_atomicfu)

	val kotlinx_coroutines = "1.8.0" // https://github.com/Kotlin/kotlinx.coroutines/releases
	module("org.jetbrains.kotlinx:kotlinx-coroutines-core", kotlinx_coroutines)
	module("org.jetbrains.kotlinx:kotlinx-coroutines-debug", kotlinx_coroutines)
	module("org.jetbrains.kotlinx:kotlinx-coroutines-test", kotlinx_coroutines)

	module("org.jetbrains.kotlinx:kotlinx-coroutines-android", kotlinx_coroutines)
	module("org.jetbrains.kotlinx:kotlinx-coroutines-swing", kotlinx_coroutines)

	//#endregion

	//#region IO support

	val okio = "3.9.0" // https://github.com/square/okio
	module("com.squareup.okio:okio", okio)

	//#endregion

	//#region Model foundations

	plugin(kotlin("plugin.serialization"), kotlin)
	module(kotlin("plugin.serialization").marker(), kotlin)

	val kotlinx_serialization = "1.6.3" // https://github.com/Kotlin/kotlinx.serialization/releases
	module("org.jetbrains.kotlinx:kotlinx-serialization-core", kotlinx_serialization)
	module("org.jetbrains.kotlinx:kotlinx-serialization-json", kotlinx_serialization)
	module("org.jetbrains.kotlinx:kotlinx-serialization-json-okio", kotlinx_serialization)
	module("org.jetbrains.kotlinx:kotlinx-serialization-cbor", kotlinx_serialization)

	val androidx_collection = "1.4.0" // https://developer.android.com/jetpack/androidx/releases/collection
	module("androidx.collection:collection", androidx_collection)

	//#endregion

	//#region UI foundations

	val androidx_core = "1.12.0" // https://developer.android.com/jetpack/androidx/releases/core
	module("androidx.core:core-ktx", androidx_core)

	val androidx_core_splashscreen = "1.0.1" // https://developer.android.com/jetpack/androidx/releases/core
	// See also,
	// - https://developer.android.com/develop/ui/views/launch/splash-screen/migrate#library
	// - https://developer.android.com/reference/kotlin/androidx/core/splashscreen/SplashScreen
	module("androidx.core:core-splashscreen", androidx_core_splashscreen)

	val androidx_activity = "1.8.2" // https://developer.android.com/jetpack/androidx/releases/activity
	module("androidx.activity:activity-ktx", androidx_activity)
	val androidx_fragment = "1.6.2" // https://developer.android.com/jetpack/androidx/releases/fragment
	module("androidx.fragment:fragment-ktx", androidx_fragment)

	val androidx_webkit = "1.9.0" // https://developer.android.com/jetpack/androidx/releases/webkit
	module("androidx.webkit:webkit", androidx_webkit)

	val flatlaf = "3.4.1" // https://github.com/JFormDesigner/FlatLaf/releases
	module("com.formdev:flatlaf", flatlaf)
	module("com.formdev:flatlaf-extras", flatlaf) // https://github.com/JFormDesigner/FlatLaf/tree/main/flatlaf-extras

	// TODO! Update as soon as issue is fixed: https://github.com/Dansoftowner/jSystemThemeDetector/issues/18
	//  - See also, https://github.com/Dansoftowner/jSystemThemeDetector/pull/30
	val jSystemThemeDetector = "3.8" // https://github.com/Dansoftowner/jSystemThemeDetector/releases
	module("com.github.Dansoftowner:jSystemThemeDetector", jSystemThemeDetector)

	// See JCEF Maven releases at https://github.com/jcefmaven/jcefmaven/releases
	prop("jcef.commit", "0b8e42e") // The "JCEF commit" indicated by the JCEF Maven release
	prop("jcef.cef", "122.1.10+gc902316+chromium-122.0.6261.112") // The "CEF version" indicated by the JCEF Maven release

	//#endregion

	//#region Test infrastructure

	val kotest = "5.8.0" // https://github.com/kotest/kotest/releases
	plugin("io.kotest.multiplatform", kotest)
	module("io.kotest.multiplatform".marker(), kotest)
	module("io.kotest:kotest-assertions-shared", kotest)
	module("io.kotest:kotest-framework-engine", kotest)
	module("io.kotest:kotest-runner-junit5", kotest)
	module("io.kotest:kotest-property", kotest)

	val assertk = "0.28.0" // https://github.com/willowtreeapps/assertk/releases
	module("com.willowtreeapps.assertk:assertk", assertk)

	//#endregion
}
