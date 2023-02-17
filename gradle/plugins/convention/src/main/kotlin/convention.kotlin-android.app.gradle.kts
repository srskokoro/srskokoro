import convention.setUpConvention

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
