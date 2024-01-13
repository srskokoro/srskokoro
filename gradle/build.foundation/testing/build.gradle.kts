@file:OptIn(build.foundation.InternalApi::class)

import build.foundation.BuildFoundation.compileOnlyTestImpl

plugins {
	id("build.conventions.support")
}

dependencies {
	compileOnlyTestImpl(gradleTestKit())
	testImplementation("build.foundation:core")
	testImplementation(kotlin("test"))
}
