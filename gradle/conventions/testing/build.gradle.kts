import org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformTestFramework
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest

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

	fun assert_JUnit5(jvmTest: TaskProvider<KotlinJvmTest>): Unit = jvmTest.configure {
		val tf = testFramework
		// Ensure that we're really using `JUnit5` (failing otherwise).
		check(tf is JUnitPlatformTestFramework) {
			"Expected `JUnitPlatformTestFramework` as test framework but was `${tf::class.qualifiedName}`"
		}
	}
}

dependencies {
	commonMainRuntimeOnly("io.kotest:kotest-assertions-shared")
	nativeMainImplementation("io.kotest:kotest-assertions-shared")
	commonMainApi("io.kotest:kotest-framework-engine", Build::excludeKotestAssertions)
	jreMainApi("io.kotest:kotest-runner-junit5", Build::excludeKotestAssertions)
	afterEvaluate { tasks.run { Build.assert_JUnit5(jreTest) } }
	commonMainApi("io.kotest:kotest-property", Build::excludeKotestAssertions)
	commonMainApi("com.willowtreeapps.assertk:assertk")
}
