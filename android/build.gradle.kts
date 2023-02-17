plugins {
	id("convention.kotlin-android.app")
	id("org.jetbrains.compose")
}

android {
	namespace = "$group.app"
	compileSdk = 33

	defaultConfig {
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
	implementation(project(":common"))
	implementation("androidx.activity:activity-compose:1.6.1")
}
