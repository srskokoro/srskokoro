import build.api.dsl.*

plugins {
	id("kokoro.build.kt.mpp.app")
	id("build.version")
	id("build.kt.x.expect_actual")
	id("build.ktx.atomicfu")
	id("build.gmazzo.buildconfig")
	id("kokoro.internal.nook")
	id("kokoro.build.jcef")
}

// NOTE: The `group` below is meant to be used by the application, such as the
// application ID used by the Android app.
group = extra["kokoro.app.group"] as String
base.archivesName = "kokoro-app"

private object Build {
	const val NAMESPACE = "kokoro.app"
}

android {
	namespace = Build.NAMESPACE
}

buildConfig {
	asPublicObject("AppBuild") inPackage Build.NAMESPACE

	val versionCode = versionCode
	if (versionCode == 0) throw InvalidUserDataException(
		"Version code 0 (zero) should not be used"
	)
	buildConfigField("String", "VERSION", "\"$versionName\"")
	buildConfigField("int", "VERSION_CODE", "$versionCode")
}

buildConfig.ss.named("jreMain") {
	asPublicObject("AppBuildDesktop") inPackage Build.NAMESPACE
	buildConfigField("String", "APP_DATA_DIR_NAME", "\"SRSKokoro${if (isReleasing) "" else "-Dev"}\"")
}

dependencies {
	commonMainApi(project(":kokoro:internal"))
	commonMainApi(project(":kokoro"))

	commonMainImplementation(kotlin("reflect"))

	commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	commonTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")

	androidMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")
	jreMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing")

	commonMainImplementation("com.squareup.okio:okio")

	commonMainImplementation(project(":kokoro.app:ui.api"))

	commonMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
	commonMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor")

	androidMainImplementation("androidx.core:core-ktx")
	androidMainImplementation("androidx.core:core-splashscreen")

	androidMainImplementation("androidx.activity:activity-ktx")
	//androidMainImplementation("androidx.fragment:fragment-ktx")
	androidMainImplementation("androidx.webkit:webkit")

	// See, "Native Libraries distribution | FlatLaf - Flat Look and Feel" --
	// https://www.formdev.com/flatlaf/native-libraries/#gradle_no_natives_jar
	jreMainImplementation("com.formdev:flatlaf::no-natives")
	jreMainImplementation("com.formdev:flatlaf-extras") {
		exclude("com.formdev", "flatlaf")
	}

	jreMainImplementation("com.github.Dansoftowner:jSystemThemeDetector")
	jreMainImplementation("org.slf4j:slf4j-jdk14") // Needed for `jSystemThemeDetector`

	jreMainImplementation(jcef.dependency)
}
