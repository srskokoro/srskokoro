plugins {
	id("build.kt.mpp.lib")
}

group = extra["kokoro.group"] as String
base.archivesName = extra["kokoro.internal.scoping.artifact"] as String
