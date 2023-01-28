plugins {
	id("convention.kotlin-android.app")
	id("org.jetbrains.compose")
}

kotlin {
	jvmToolchain(cfgs.jvm.toolchainConfig)
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

	compileOptions {
		// TODO Remove eventually -- See, https://issuetracker.google.com/issues/260059413
		cfgs.jvm.verObj.let {
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

dependencies {
	testImplementation("io.kotest:kotest-runner-junit5")
	deps.bundles.testCommon {
		testImplementation(it)
	}
	implementation(project(":common"))
	implementation("androidx.activity:activity-compose:1.6.1")
}
