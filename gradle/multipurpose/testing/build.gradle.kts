import org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformTestFramework

plugins {
	id("build.kt.mpp.multipurpose")
}

dependencies {
	commonMainApi("io.kotest:kotest-framework-engine")
	jvmMainApi("io.kotest:kotest-runner-junit5")
	// Ensure that we're really using `JUnit5` (failing otherwise).
	tasks.jvmTest { check(testFramework is JUnitPlatformTestFramework) }

	commonMainApi("com.willowtreeapps.assertk:assertk")
}
