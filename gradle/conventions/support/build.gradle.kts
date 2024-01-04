plugins {
	id("build.conventions")
}

dependencies {
	api("build.foundation:support")
	testImplementation(project(":testing.conventions"))
}
