plugins {
	id("kokoro.build.kt.mpp.app")
	id("kokoro.build.kt.js")
}

dependencies {
	commonMainApi(project(":kokoro:internal"))
}
