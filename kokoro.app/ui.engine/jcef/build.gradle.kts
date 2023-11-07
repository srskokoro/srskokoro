plugins {
	id("kokoro.conv.kt.jvm")
	id("kokoro.jcef.dependency")
}

dependencies {
	implementation(project(":kokoro.lib.internal"))
	api(jcef.dependency)
}
