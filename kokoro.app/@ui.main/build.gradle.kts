import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackOutput

plugins {
	id("kokoro.build.kt.js.packed")
}

base.archivesName = "ui"

kotlin.js {
	browser {
		webpackTask {
			output.libraryTarget = KotlinWebpackOutput.Target.WINDOW
		}
	}
}

dependencies {
	jsMainImplementation(project(":kokoro.app:ui.api"))
}
