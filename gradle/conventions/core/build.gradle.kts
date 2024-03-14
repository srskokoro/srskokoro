plugins {
	id("build.conventions")
}

dependencies {
	implementation("build.foundation:complement")
	implementation("build.foundation:dependencies")

	implementation(plugin("com.github.johnrengelman.shadow"))

	api(project(":support.kotlin-gradle-plugin"))
	implementation(kotlin("sam-with-receiver"))
	implementation(kotlin("assignment"))

	implementation("org.jetbrains.kotlinx:atomicfu-gradle-plugin")
	implementation("com.android.tools.build:gradle")
	implementation(plugin("io.kotest.multiplatform"))
	implementation(plugin("com.github.gmazzo.buildconfig"))

	testImplementation(project(":testing.conventions"))
}
