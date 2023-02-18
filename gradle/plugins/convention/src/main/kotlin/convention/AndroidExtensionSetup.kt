package convention

import convention.internal.AndroidExtension
import deps

internal fun setUp(extension: AndroidExtension): Unit = with(extension) {
	compileOptions {
		// TODO Remove eventually -- See, https://issuetracker.google.com/issues/260059413
		deps.jvm.verObj.let {
			sourceCompatibility = it
			targetCompatibility = it
		}
	}

	testOptions {
		unitTests.all {
			setUp(it)
		}
	}
}
