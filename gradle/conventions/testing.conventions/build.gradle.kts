import build.api.dsl.accessors.compileOnlyTestImpl

plugins {
	id("build.conventions")
}

dependencies {
	api("build.foundation:support")
	api("build.foundation:testing")
	api(project(":testing"))
	compileOnlyTestImpl(gradleTestKit())
}
