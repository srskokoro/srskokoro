plugins {
	id("kokoro.conv.kt.mpp.lib")
}

dependencies {
	desktopJvmMainApi(project(":kokoro.app:ui.engine:jcef"))

	commonMainImplementation(project(":kokoro:internal"))

	androidMainApi("androidx.webkit:webkit")
	appMainImplementation("com.squareup.okio:okio")
}
