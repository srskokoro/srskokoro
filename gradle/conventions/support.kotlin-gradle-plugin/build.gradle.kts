plugins {
	id("build.conventions")
}

dependencies {
	api(kotlin("gradle-plugin"))
	implementation("com.android.tools.build:gradle")

	api(project(":support"))
	testImplementation(project(":testing.conventions"))
}
