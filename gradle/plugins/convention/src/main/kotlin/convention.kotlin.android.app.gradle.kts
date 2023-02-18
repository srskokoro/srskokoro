import convention.*

plugins {
	id("convention.base")
	id("com.android.application")
	kotlin("android")
}

kotlin {
	setUp(this)
}

android {
	setUp(this)

	packagingOptions {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
}

dependencies {
	setUpTestFrameworkDeps_android {
		testImplementation(it)
	}
	setUpTestCommonDeps {
		testImplementation(it)
	}
}
