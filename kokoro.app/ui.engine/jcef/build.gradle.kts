plugins {
	id("kokoro.conv.kt.jvm")
	id("kokoro.jcef.dependency")
}

dependencies {
	implementation(project(":kokoro.app:core.base"))
	implementation(project(":kokoro.lib:internal"))
	implementation("androidx.annotation:annotation")
	api(jcef.dependency)
}
