import convention.setUpConvention
import convention.setUpTestCommonDeps
import convention.setUpTestFrameworkDeps_android

plugins {
	id("convention.base")
	id("com.android.application")
	kotlin("android")
}

kotlin {
	setUpConvention()
}

android {
	setUpConvention()

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
