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
			runtimeOnly("io.kotest:kotest-framework-engine")
		}
	}
	targets {
		withType<KotlinAndroidTarget>().configureEach {
			sourceSets.named("${targetName}UnitTest") {
				dependencies {
					runtimeOnly("io.kotest:kotest-runner-junit5")
				}
			}
		}
		withType<KotlinJvmTarget>().configureEach {
			testRuns["test"].executionTask.configure(Test::setUp)
			compilations.named("test") {
				defaultSourceSet.dependencies {
					runtimeOnly("io.kotest:kotest-runner-junit5")
				}
			}
		}
	}
}

extensions.findByName("android")?.run {
	if (this !is com.android.build.api.dsl.CommonExtension<*, *, *, *, *>) return@run

	@Suppress("UnstableApiUsage")
	testOptions {
		unitTests.all(Test::setUp)
	}
}
