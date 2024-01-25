plugins {
	id("build.conventions")
}

dependencies {
	implementation("build.foundation:complement")
	implementation("build.foundation:dependencies")

	api(project(":support.kotlin-gradle-plugin"))
	implementation(kotlin("sam-with-receiver"))
	implementation(kotlin("assignment"))
	implementation("com.android.tools.build:gradle")
	implementation(plugin("io.kotest.multiplatform"))

	testImplementation(project(":testing.conventions"))
}
