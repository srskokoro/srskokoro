plugins {
	id("com.android.application")
	kotlin("android")
	id("org.jetbrains.compose")
}

android {
	namespace = "com.myapplication"
	compileSdk = 33

	defaultConfig {
		minSdk = 21
		targetSdk = 33
		versionCode = 1
		versionName = "1.0"
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}

	packagingOptions {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
	buildTypes {
		getByName("release") {
			isMinifyEnabled = false
		}
	}
}

dependencies {
	implementation(project(":common"))
	implementation("androidx.activity:activity-compose:1.6.1")
}
