plugins {
	id("build.conventions")
}

dependencies {
	api(kotlin("gradle-plugin"))
	api("com.android.tools.build:gradle")

	api(project(":support"))
	testImplementation(project(":testing.conventions"))
}
