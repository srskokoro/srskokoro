plugins {
	id("com.android.application")
	kotlin("android")
	id("org.jetbrains.compose")
	id("build-support")
}

val kotlinOptJvmTarget: String by rootProject.extra
val javaVer: JavaVersion by rootProject.extra
val javaToolchainConfig: Action<JavaToolchainSpec> by rootProject.extra

kotlin {
	jvmToolchain(javaToolchainConfig)
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

	kotlinOptions {
		jvmTarget = kotlinOptJvmTarget
	}
	compileOptions {
		javaVer.let {
			sourceCompatibility = it
			targetCompatibility = it
		}
	}
	testOptions {
		unitTests.all {
			it.useJUnitPlatform()
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

// TEST dependencies ONLY
dependencies {
	testImplementation(libs.kotest.runner.junit5)
	testImplementation(libs.bundles.test.common)
}

// MAIN dependencies
dependencies {
	implementation(project(":common"))
	implementation("androidx.activity:activity-compose:1.6.1")
}
