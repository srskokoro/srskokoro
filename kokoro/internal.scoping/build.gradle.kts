plugins {
	id("build.kt.mpp.lib")
}

group = extra["kokoro.group"] as String
base.archivesName = "kokoro-internal-scoping"