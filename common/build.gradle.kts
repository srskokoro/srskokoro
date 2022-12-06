plugins {
	id("com.android.library")
	kotlin("multiplatform")
	id("org.jetbrains.compose")
}

kotlin {
	android()
	jvm("desktop")

	sourceSets {
		named("commonMain") {
			dependencies {
				api(compose.runtime)
				api(compose.foundation)
				api(compose.material)
				// Needed only for preview.
				implementation(compose.preview)
			}
		}
		named("androidMain") {
			dependencies {
				api("androidx.activity:activity-compose:1.5.0")
				api("androidx.appcompat:appcompat:1.5.1")
				api("androidx.core:core-ktx:1.8.0")
			}
		}
	}
}

android {
	namespace = "com.myapplication.common"
	compileSdk = 33

	defaultConfig {
		minSdk = 21
		targetSdk = 33
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}

	sourceSets {
		named("main") {
			manifest.srcFile("src/androidMain/AndroidManifest.xml")
			res.srcDirs("src/androidMain/res")
		}
	}
}
