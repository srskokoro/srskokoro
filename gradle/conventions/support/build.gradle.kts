plugins {
	id("build.conventions")
}

dependencies {
	api("build.support:support")
	testImplementation(project(":testing.conventions"))
}
