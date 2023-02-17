import convention.configureConvention
import convention.configureTestTask

plugins {
	id("convention.base")
	id("com.android.application")
	kotlin("android")
}

kotlin {
	configureConvention()
}

android {
	configureConvention()

	testOptions {
		unitTests.all {
			it.configureTestTask()
		}
	}

	packagingOptions {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
}
