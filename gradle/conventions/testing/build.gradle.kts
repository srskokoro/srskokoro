import org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformTestFramework

plugins {
	id("build.foundation.kt.mpp.inclusive")
}

private object Build {
	/**
	 * Use this utility to exclude `kotest-assertions-shared` from a dependency.
	 *
	 * NOTE: At the moment, both Kotest framework and its property testing
	 * library brings in `kotest-assertions-shared` (which brings in `shouldBe`,
	 * `shouldNotBe`, etc.) when we just want the test framework and the
	 * property testing library (as we want a different assertion library).
	 */
	fun excludeKotestAssertions(dependency: ExternalModuleDependency) {
		dependency.exclude("io.kotest", "kotest-assertions-shared")
	}
}

dependencies {
	commonMainRuntimeOnly("io.kotest:kotest-assertions-shared")
	commonMainApi("io.kotest:kotest-framework-engine", Build::excludeKotestAssertions)
	jvmMainApi("io.kotest:kotest-runner-junit5") {
		Build.excludeKotestAssertions(this)
		// Ensure that we're really using `JUnit5` (failing otherwise).
		tasks.jvmTest { check(testFramework is JUnitPlatformTestFramework) }
	}

	commonMainApi("com.willowtreeapps.assertk:assertk")
}
