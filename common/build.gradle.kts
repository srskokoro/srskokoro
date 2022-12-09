plugins {
	id("com.android.library")
	kotlin("multiplatform")
	id("io.kotest.multiplatform")
	id("org.jetbrains.compose")
}

val kotlinOptJvmTarget: String by rootProject.extra
val javaVer: JavaVersion by rootProject.extra
val javaToolchainConfig: Action<JavaToolchainSpec> by rootProject.extra

kotlin {
	jvmToolchain(javaToolchainConfig)

	android {
		compilations.all {
			// See, https://stackoverflow.com/a/67024907
			kotlinOptions.jvmTarget = kotlinOptJvmTarget
		}
	}
	jvm("desktop") {
		compilations.all {
			// TODO Remove eventually -- See, https://github.com/JetBrains/compose-jb/issues/2511
			kotlinOptions.jvmTarget = kotlinOptJvmTarget
		}
		testRuns["test"].executionTask.configure {
			useJUnitPlatform()
		}
	}

	// TEST source sets ONLY
	sourceSets {
		named("commonTest") {
			dependencies {
				implementation(libs.kotest.framework.engine)
				implementation(libs.bundles.test.common)
			}
		}
		named("desktopTest") {
			dependencies {
				implementation(libs.kotest.runner.junit5)
			}
		}

		// Remove log pollution until Android support in KMP improves
		// - See, https://discuss.kotlinlang.org/t/21448
		setOf(
			"androidAndroidTestRelease",
			"androidTestFixtures",
			"androidTestFixturesDebug",
			"androidTestFixturesRelease",
		).also { excl ->
			removeAll { it.name in excl }
		}
	}

	// MAIN source sets
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
