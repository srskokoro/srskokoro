plugins {
	id("kokoro.conv.kt.mpp.lib")
	id("conv.kt.mpp.assets")
}

dependencies {
	commonMainImplementation(project(":kokoro.lib.internal"))

	appMainImplementation("com.squareup.okio:okio")
}
