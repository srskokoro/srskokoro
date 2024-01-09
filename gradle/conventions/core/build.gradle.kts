plugins {
	id("build.conventions")
}

dependencies {
	implementation("build.foundation:complement")
	implementation("build.foundation:dependencies")

	implementation(project(":support.kotlin-gradle-plugin"))
	implementation(kotlin("sam-with-receiver"))
	implementation(kotlin("assignment"))
	implementation("io.kotest:kotest-framework-multiplatform-plugin-gradle")

	testImplementation(project(":testing.conventions"))
}
