import convention.configureConvention

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
			it.useJUnitPlatform()
		}
	}

	packagingOptions {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
}
