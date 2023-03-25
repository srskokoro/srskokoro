package convention.internal.setup

import com.android.build.api.dsl.ApplicationBaseFlavor
import deps

internal fun setUp(android: AndroidExtension): Unit = with(android) {
	compileSdk = 33

	defaultConfig {
		if (this is ApplicationBaseFlavor) {
			targetSdk = 33
		}
		minSdk = 21
	}

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
