plugins {
	id("build.conventions")
}

dependencies {
	implementation(project(":support"))
	testImplementation(project(":testing"))
	testImplementation(kotlin("test"))
}
