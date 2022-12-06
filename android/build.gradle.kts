plugins {
	id("com.android.application")
	kotlin("android")
	id("org.jetbrains.compose")
}

android {
	namespace = "com.myapplication"
	compileSdk = 32

	defaultConfig {
		minSdk = 26
		targetSdk = 32
		versionCode = 1
		versionName = "1.0"
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
}

dependencies {
	implementation(project(":common"))
	implementation("androidx.activity:activity-compose:1.5.0")
}
