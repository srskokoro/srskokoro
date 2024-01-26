import build.api.dsl.*

plugins {
	id("com.android.application")
	id("build.kt.android.app")
	id("build.version")
}

group = evaluatedParent.group

android {
	namespace = "kokoro.app.target"

	defaultConfig {
		applicationId = "$group.app"
		applicationIdSuffix = ".dev".takeUnless { projectThis.isReleasing }
		versionName = projectThis.versionName
		versionCode = projectThis.versionCode
	}

	buildTypes {
		release {
			isMinifyEnabled = false
		}
	}
}

dependencies {
	implementation(project(":kokoro.app"))
}
