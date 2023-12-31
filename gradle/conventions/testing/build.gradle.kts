import build.api.dsl.accessors.compileOnlyTestImpl

plugins {
	id("build.conventions")
}

dependencies {
	api("build.support:support")
	api("build.support:testing")
	api("com.willowtreeapps.assertk:assertk")
	compileOnlyTestImpl(gradleTestKit())
}
