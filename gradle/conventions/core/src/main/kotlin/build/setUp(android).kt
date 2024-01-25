package build

import build.api.dsl.*
import com.android.build.api.dsl.ApplicationBaseFlavor
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

internal fun Project.setUp(android: AndroidExtension) {
	android.run {
		deps?.run {
			buildToolsVersion = prop("build.android.buildToolsVersion")
			compileSdk = prop("build.android.compileSdk").toInt()

			defaultConfig.run {
				val targetSdkValue = prop("build.android.targetSdk").toInt()
				if (this is ApplicationBaseFlavor) {
					targetSdk = targetSdkValue
				} else {
					lint.targetSdk = targetSdkValue
				}
				minSdk = prop("build.android.minSdk").toInt()
			}
		}
	}

	android.compileOptions.run {
		isCoreLibraryDesugaringEnabled = true
	}
	dependencies.run {
		add("coreLibraryDesugaring", "com.android.tools:desugar_jdk_libs_nio")
	}
}
