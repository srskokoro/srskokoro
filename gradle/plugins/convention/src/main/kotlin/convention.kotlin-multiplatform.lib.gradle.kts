import convention.setUpConvention

plugins {
	id("com.android.library")
	id("convention.kotlin-multiplatform.base")
}

android {
	setUpConvention()
}
