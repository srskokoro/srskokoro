plugins {
	id("build.conventions.support")
}

dependencies {
	testImplementation("build.foundation:core")
	testImplementation(project(":testing"))
}
