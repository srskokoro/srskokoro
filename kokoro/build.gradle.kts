plugins {
	id("kokoro.build.kt.mpp.lib")
}

group = extra["kokoro.group"] as String
base.archivesName = "kokoro"

android {
	namespace = "kokoro"
}

dependencies {
	commonMainImplementation(project(":kokoro:internal"))
}
