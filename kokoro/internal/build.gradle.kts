import build.api.dsl.*

plugins {
	id("kokoro.build.kt.mpp.lib")
	id("kokoro.build.kt.js")
	id("build.kt.x.contracts")
	id("build.kt.x.expect_actual")
	id("build.ktx.atomicfu")
	id("build.gmazzo.buildconfig")
}

group = extra["kokoro.group"] as String
base.archivesName = "kokoro-internal"

private object Build {
	const val NAMESPACE = "kokoro.internal"
}

android {
	namespace = Build.NAMESPACE
}

buildConfig {
	asPublicTopLevel() inPackage Build.NAMESPACE

	val isReleasing = isReleasing
	require(isDebug == !isReleasing) {
		throw AssertionError("Required: `${::isDebug.name} == !${::isReleasing.name}`")
	}
	buildConfigField("Boolean", "IS_RELEASING", isReleasing)
	buildConfigField("Boolean", "RELEASE", "IS_RELEASING")
	buildConfigField("Boolean", "DEBUG", "!RELEASE")
}

dependencies {
	hostMainApi("androidx.annotation:annotation")
	commonMainApi("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	commonMainApi("com.squareup.okio:okio")
	commonMainApi("org.jetbrains.kotlinx:kotlinx-serialization-core")
	hostMainApi("androidx.collection:collection")
}
