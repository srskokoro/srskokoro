plugins {
	id("kokoro.conv.kt.mpp.lib")
}

dependencies {
	val unsafeParent = unsafeParent
	desktopJvmMainApi(unsafeParent.project("jcef"))

	commonMainImplementation(project(":kokoro.lib.internal"))

	androidMainApi("androidx.webkit:webkit")
	appMainImplementation("com.squareup.okio:okio")
}
