plugins {
	id("build.conventions")
}

dependencies {
	implementation(project(":support"))
	api("org.gradle.toolchains:foojay-resolver:${project.extra["build.foojay"]}")
}
