import convention.configureConvention
import convention.configureTestTask

plugins {
	id("com.android.library")
	id("convention.kotlin-multiplatform.base")
}

android {
	configureConvention()

	testOptions {
		unitTests.all {
			it.configureTestTask()
		}
	}
}
