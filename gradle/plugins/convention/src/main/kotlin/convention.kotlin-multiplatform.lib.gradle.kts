import convention.configureConvention

plugins {
	id("com.android.library")
	id("convention.kotlin-multiplatform.base")
}

android {
	configureConvention()
}
