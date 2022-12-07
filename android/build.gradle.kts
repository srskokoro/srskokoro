plugins {
	id("com.android.application")
	kotlin("android")
	id("org.jetbrains.compose")
}

val javaVer: JavaVersion by rootProject.extra
val javaToolchainConfig: Action<JavaToolchainSpec> by rootProject.extra

java {
	toolchain(javaToolchainConfig)
}

kotlin {
	jvmToolchain(javaToolchainConfig)
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
		javaVer.let {
			sourceCompatibility = it
			targetCompatibility = it
		}
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
