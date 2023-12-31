plugins {
	id("build.conventions")
}

dependencies {
	api(kotlin("gradle-plugin"))
	api(project(":support"))
	testImplementation(project(":testing"))
}
