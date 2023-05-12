plugins {
	id("kokoro.conv.kt.mpp.lib.sub")
}

dependencies {
	commonMainImplementation(project(":kokoro.lib.internal"))
	appMainImplementation("com.squareup.okio:okio")
}
