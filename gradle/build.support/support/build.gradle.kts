plugins {
	id("build.conventions.support")
}

dependencies {
	testImplementation(":build.foundation")
	testImplementation(project(":testing"))
}
