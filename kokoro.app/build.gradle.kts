plugins {
	id("kokoro.build.kt.mpp.app")
	id("build.kt.x.expect_actual")
	id("build.ktx.atomicfu")
	id("kokoro.internal.nook")
}

// NOTE: The `group` below is meant to be used by the application, such as the
// application ID used by the Android app.
group = extra["kokoro.app.group"] as String
base.archivesName = "kokoro-app"

android {
	namespace = "kokoro.app"

	defaultConfig.manifestPlaceholders.let { map ->
		map["APP_TITLE"] = extra["kokoro.app.title"] as String
		map["APP_TITLE_SHORT"] = extra["kokoro.app.title.short"] as String
	}
}

dependencies {
	commonMainApi(project(":kokoro:internal"))
	commonMainApi(project(":kokoro"))

	commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	commonTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")

	androidMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")
	jreMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing")

	commonMainImplementation(project(":kokoro.app:ui.api"))

	commonMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
	commonMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor")

	androidMainImplementation("androidx.core:core-ktx")
	androidMainImplementation("androidx.core:core-splashscreen")

	androidMainImplementation("androidx.activity:activity-ktx")
	//androidMainImplementation("androidx.fragment:fragment-ktx")
	androidMainImplementation("androidx.webkit:webkit")
}
