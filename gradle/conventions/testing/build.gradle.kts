import build.api.dsl.accessors.compileOnlyTestImpl
import org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformTestFramework

plugins {
	id("build.conventions")
}

dependencies {
	api("build.support:support")
	api("build.support:testing")

	api("io.kotest:kotest-runner-junit5")
	// Ensure that we're really using `JUnit5` (failing otherwise).
	tasks.test { check(testFramework is JUnitPlatformTestFramework) }

	api("com.willowtreeapps.assertk:assertk")
	compileOnlyTestImpl(gradleTestKit())
}
