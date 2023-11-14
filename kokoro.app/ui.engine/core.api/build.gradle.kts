plugins {
	id("kokoro.conv.kt.mpp.lib")
}

dependencies {
	commonMainImplementation(project(":kokoro.lib.internal"))

	androidMainApi("androidx.webkit:webkit")
	appMainImplementation("com.squareup.okio:okio")
}
