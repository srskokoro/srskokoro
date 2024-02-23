import build.api.dsl.*
import build.foundation.BuildFoundation
import build.foundation.BuildFoundation.MPP
import org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformTestFramework
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest

plugins {
	id("build.foundation.kt.mpp.inclusive")
	id("io.kotest.multiplatform")
}

private object Build {
	const val KOTEST_ASSERTIONS_SHARED = "kotest-assertions-shared"

	/**
	 * Use this utility to exclude `kotest-assertions-shared` from a dependency.
	 *
	 * NOTE: At the moment, both Kotest framework and its property testing
	 * library brings in `kotest-assertions-shared` (which brings in `shouldBe`,
	 * `shouldNotBe`, etc.) when we just want the test framework and the
	 * property testing library (as we want a different assertion library).
	 */
	fun excludeKotestAssertionsShared(dependency: ExternalModuleDependency) {
		dependency.exclude("io.kotest", KOTEST_ASSERTIONS_SHARED)
	}

	fun assert_JUnit5(jvmTest: TaskProvider<KotlinJvmTest>): Unit = jvmTest.configure {
		val tf = testFramework
		// Ensure that we're really using `JUnit5` (failing otherwise).
		check(tf is JUnitPlatformTestFramework) {
			"Expected `JUnitPlatformTestFramework` as test framework but was `${tf::class.qualifiedName}`"
		}
	}
}

@OptIn(build.foundation.InternalApi::class)
kotlin {
	jvm(MPP.jre)

	if (BuildFoundation.shouldBuildJs(projectThis)) js(IR) {
		browser()
		nodejs()
	}

	if (BuildFoundation.shouldBuildNative(projectThis)) {
		iosX64()
		iosArm64()
		iosSimulatorArm64()
	} else {
		configurations.create("nativeMainImplementation")
	}
}

dependencies {
	commonMainRuntimeOnly("io.kotest:${Build.KOTEST_ASSERTIONS_SHARED}")
	"nativeMainImplementation"("io.kotest:${Build.KOTEST_ASSERTIONS_SHARED}")
	commonMainApi("io.kotest:kotest-framework-engine", Build::excludeKotestAssertionsShared)
	"jreMainApi"("io.kotest:kotest-runner-junit5", Build::excludeKotestAssertionsShared)
	afterEvaluate { tasks.run { Build.assert_JUnit5(named<KotlinJvmTest>("jreTest")) } }
	commonMainApi("io.kotest:kotest-property", Build::excludeKotestAssertionsShared)
	commonMainApi("com.willowtreeapps.assertk:assertk")
}
