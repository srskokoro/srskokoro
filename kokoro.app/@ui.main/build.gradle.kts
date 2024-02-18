plugins {
	id("kokoro.build.kt.js.packed")
}

base.archivesName = "ui"

dependencies {
	jsMainImplementation(project(":kokoro.app:ui.api"))
}
