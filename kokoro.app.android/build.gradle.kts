plugins {
	id("conv.kt.android.app")
	id("conv.version")
}

android {
	namespace = extra["kokoro.app.target.ns"] as String

	defaultConfig {
		applicationId = "$group.app"
		applicationIdSuffix = ".dev".takeUnless { project.isReleasing }
		versionName = project.versionName
		versionCode = project.versionCode
	}

	buildTypes {
		release {
			isMinifyEnabled = false
		}
	}
}

dependencies {
	deps.bundles.testExtras *= {
		testImplementation(it)
	}
	implementation(project(":kokoro.app"))
}
