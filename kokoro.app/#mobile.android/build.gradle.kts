import build.api.dsl.*

plugins {
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

		manifestPlaceholders.let { map ->
			map["APP_TITLE"] = extra["kokoro.app.title"] as String
			map["APP_TITLE_SHORT"] = extra["kokoro.app.title.short"] as String
		}
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
