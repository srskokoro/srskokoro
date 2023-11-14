plugins {
	id("kokoro.conv.kt.android.app")
	id("conv.version")
}

group = evaluatedParent.group

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
	implementation(project(":kokoro.app"))
}
