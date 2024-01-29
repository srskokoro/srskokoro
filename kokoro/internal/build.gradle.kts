plugins {
	id("kokoro.build.kt.mpp.lib")
}

group = extra["kokoro.group"] as String
base.archivesName = "kokoro-internal"

android {
	namespace = "kokoro.internal"
}
