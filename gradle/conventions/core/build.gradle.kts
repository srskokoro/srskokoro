plugins {
	id("build.conventions")
}

dependencies {
	implementation(":build.base")

	implementation(project(":support.kotlin-gradle-plugin"))
	implementation(kotlin("sam-with-receiver"))
	implementation(kotlin("assignment"))

	testImplementation(project(":testing"))
}
