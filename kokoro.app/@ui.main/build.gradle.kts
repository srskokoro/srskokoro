import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput

plugins {
	id("kokoro.build.kt.js.packed")
	id("build.gmazzo.buildconfig")
}

private object Build {
	const val NAMESPACE = "kokoro.app.ui.engine"
	const val ARCHIVE_NAME = "ui"
	const val UI_MODULE_NAME = "uiJs"
}

base.archivesName = Build.ARCHIVE_NAME

kotlin.js {
	browser {
		webpackTask {
			output.library = Build.UI_MODULE_NAME
			output.libraryTarget = KotlinWebpackOutput.Target.WINDOW
		}
	}
}

buildConfig {
	asPublicTopLevel() inPackage Build.NAMESPACE
	buildConfigField("String", "UI_MODULE", "\"${Build.UI_MODULE_NAME}\"")
}

dependencies {
	jsMainImplementation(project(":kokoro.app:ui.api"))
}
