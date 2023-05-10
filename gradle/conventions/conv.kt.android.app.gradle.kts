import conv.internal.setup.*

plugins {
	id("conv.base")
	id("com.android.application")
	kotlin("android")
}

kotlin {
	setUp(this)
}

android {
	setUp(this)
	kotlinOptions { setUp(options) }

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
