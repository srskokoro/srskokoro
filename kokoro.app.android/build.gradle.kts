plugins {
	id("convention.kotlin.android.app")
}

android {
	namespace = extra["srs.kokoro.app.target.ns"] as String

	defaultConfig {
		applicationId = "$group.app"
		versionCode = 1
		versionName = "1.0"
	}

	buildTypes {
		release {
			@Suppress("UnstableApiUsage")
			isMinifyEnabled = false
		}
	}
}

dependencies {
	deps.bundles.testExtras {
		testImplementation(it)
	}
	implementation(project(":kokoro.app"))
}
