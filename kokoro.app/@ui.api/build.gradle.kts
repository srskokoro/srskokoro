plugins {
	id("kokoro.build.kt.mpp.app")
	id("kokoro.build.kt.js")
	id("build.kt.x.expect_actual")
}

dependencies {
	commonMainApi(project(":kokoro:internal"))
	commonMainApi("org.jetbrains.kotlinx:kotlinx-serialization-core")
	commonMainApi("org.jetbrains.kotlinx:kotlinx-html")
}
