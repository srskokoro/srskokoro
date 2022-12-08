plugins {
	id("com.android.library")
	kotlin("multiplatform")
	id("org.jetbrains.compose")
}

val javaVer: JavaVersion by rootProject.extra
val javaToolchainConfig: Action<JavaToolchainSpec> by rootProject.extra

kotlin {
	jvmToolchain(javaToolchainConfig)

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
				api("androidx.core:core-ktx:1.9.0")
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
		javaVer.let {
			sourceCompatibility = it
			targetCompatibility = it
		}
	}

	sourceSets {
		named("main") {
			manifest.srcFile("src/androidMain/AndroidManifest.xml")
			res.srcDirs("src/androidMain/res")
		}
	}
}
