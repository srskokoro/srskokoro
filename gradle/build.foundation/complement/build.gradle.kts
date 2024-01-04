plugins {
	id("build.conventions")
}

tasks.compileKotlin.configure {
	compilerOptions.freeCompilerArgs.add("-opt-in=build.foundation.InternalApi")
}

dependencies {
	api("build.foundation:core")
	implementation(project(":support"))
	testImplementation(project(":testing"))
	testImplementation(kotlin("test"))
}
