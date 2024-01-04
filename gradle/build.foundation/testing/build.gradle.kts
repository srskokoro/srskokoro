@file:OptIn(build.foundation.InternalApi::class)

import build.foundation.compileOnlyTestImpl

plugins {
	id("build.conventions.support")
}

dependencies {
	compileOnlyTestImpl(gradleTestKit())
	testImplementation("build.foundation:core")
	testImplementation(kotlin("test"))
}
