import build.api.dsl.*

plugins {
	id("build.kt.jvm.app")
	id("build.version")
}

group = evaluatedParent.group

application {
	mainClass.set("main.MainKt")
}

dependencies {
	implementation(project(":kokoro.app"))
}
