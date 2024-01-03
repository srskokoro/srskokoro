@file:OptIn(build.conventions.internal.InternalConventionsApi::class)

import build.conventions.internal.compileOnlyTestImpl
import org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformTestFramework

plugins {
	id("build.conventions.support")
}

dependencies {
	compileOnlyTestImpl(gradleTestKit())

	// NOTE: A dependency on `kotlin("test")` should be enough if configuration
	// `testImplementation` is used: a dependency on `kotlin("test-junit5")`
	// should be added automatically in that case (provided that the `test` task
	// is configured to use JUnit5). However, that won't be the case if we're
	// using `implementation` (or `api`) configuration.
	api(kotlin("test-junit5"))
	// Ensure that we're really using `JUnit5` (failing otherwise).
	tasks.test { check(testFramework is JUnitPlatformTestFramework) }

	testImplementation(":build.foundation")
}
