import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
	id("com.android.library")
	id("convention.kotlin-multiplatform.base")
}

kotlin {
	jvmToolchain(deps.jvm.toolchainConfig)

	targets.withType<KotlinJvmTarget> {
		testRuns["test"].executionTask.configure {
			useJUnitPlatform()
		}
	}
}

android {
	compileOptions {
		// TODO Remove eventually -- See, https://issuetracker.google.com/issues/260059413
		deps.jvm.verObj.let {
			sourceCompatibility = it
			targetCompatibility = it
		}
	}
}
