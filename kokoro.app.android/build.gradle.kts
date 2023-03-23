plugins {
	id("convention.kotlin.android.app")
}

android {
	namespace = extra["srs.kokoro.app.ns"] as String
	compileSdk = 33

	defaultConfig {
		applicationId = "$group.app"
		minSdk = 21
		targetSdk = 33
		versionCode = 1
		versionName = "1.0"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
		}
	}
}

dependencies {
	deps.bundles.testExtras {
		testImplementation(it)
	}
	implementation(project(":kokoro.app"))
	implementation("androidx.activity:activity-ktx:1.6.1")
}
