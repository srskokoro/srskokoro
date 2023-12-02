plugins {
	id("kokoro.conv.kt.mpp.lib")
}

dependencies {
	commonMainImplementation(project(":kokoro:internal"))

	androidMainApi("androidx.webkit:webkit")
	desktopJvmMainApi(project(":kokoro.app:ui.engine:jcef"))

	appMainImplementation("com.squareup.okio:okio")
}
