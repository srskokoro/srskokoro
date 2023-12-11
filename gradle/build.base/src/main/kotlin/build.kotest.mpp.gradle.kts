import build.kotest.setUp
import build.kotest.setUpTestDependencies
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
	kotlin("multiplatform")
	id("io.kotest.multiplatform")
}

kotlin {
	val sourceSets = sourceSets
	sourceSets.commonTest {
		dependencies {
			project.setUpTestDependencies(::implementation)
			implementation("io.kotest:kotest-framework-engine")
		}
	}
	targets {
		withType<KotlinAndroidTarget>().configureEach {
			sourceSets.named("${targetName}UnitTest") {
				dependencies {
					implementation("io.kotest:kotest-runner-junit5")
				}
			}
		}
		withType<KotlinJvmTarget>().configureEach {
			testRuns["test"].executionTask.configure(Test::setUp)
			compilations.named("test") {
				defaultSourceSet.dependencies {
					implementation("io.kotest:kotest-runner-junit5")
				}
			}
		}
	}
}

// NOTE: The cast below throws on non-null incompatible types (as intended).
(extensions.findByName("android") as com.android.build.api.dsl.CommonExtension<*, *, *, *, *>?)?.run {
	@Suppress("UnstableApiUsage")
	testOptions {
		unitTests.all(Test::setUp)
	}
}
