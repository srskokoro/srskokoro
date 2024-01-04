plugins {
	id("build.conventions")
}

dependencies {
	api("build.foundation:core")
	implementation(project(":support"))
}
