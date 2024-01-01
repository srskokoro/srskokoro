plugins {
	id("build.conventions.support")
}

dependencies {
	testImplementation(":build.base")
	testImplementation(project(":testing"))
}
