package convention

import deps

internal fun setUp(android: AndroidExtension): Unit = with(android) {
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
