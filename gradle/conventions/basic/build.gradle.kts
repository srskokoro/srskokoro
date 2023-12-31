plugins {
	id("build.conventions")
}

dependencies {
	implementation(":build.base")
	implementation(project(":support.kotlin-gradle-plugin"))
	testImplementation(project(":testing"))
}
