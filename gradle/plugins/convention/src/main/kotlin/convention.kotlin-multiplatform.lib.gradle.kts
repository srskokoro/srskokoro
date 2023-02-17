plugins {
	id("com.android.library")
	id("convention.kotlin-multiplatform.base")
}

android {
	compileOptions {
		// TODO Remove eventually -- See, https://issuetracker.google.com/issues/260059413
		deps.jvm.verObj.let {
			sourceCompatibility = it
			targetCompatibility = it
		}
	}
}
