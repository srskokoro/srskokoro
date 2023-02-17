package convention

import com.android.build.api.dsl.CommonExtension
import deps

fun CommonExtension<*, *, *, *>.setUpConvention() {
	compileOptions {
		// TODO Remove eventually -- See, https://issuetracker.google.com/issues/260059413
		deps.jvm.verObj.let {
			sourceCompatibility = it
			targetCompatibility = it
		}
	}

	testOptions {
		unitTests.all {
			it.setUpTestTask()
		}
	}
}
