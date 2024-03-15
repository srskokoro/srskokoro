import build.api.dsl.*
import build.support.cast

plugins {
	id("build.kt.jvm.app")
	id("build.version")
}

group = evaluatedParent.group

application {
	mainClass.set("main.MainKt")
	applicationName = extra["kokoro.app.exe.name"] as String
}

tasks.startShadowScripts {
	// Prefer the "start scripts" template provided by Gradle.
	val startScripts = project.tasks.startScripts.get()
	unixStartScriptGenerator.cast<TemplateBasedScriptGenerator>().template =
		startScripts.unixStartScriptGenerator.cast<TemplateBasedScriptGenerator>().template
	windowsStartScriptGenerator.cast<TemplateBasedScriptGenerator>().template =
		startScripts.windowsStartScriptGenerator.cast<TemplateBasedScriptGenerator>().template
}

dependencies {
	implementation(project(":kokoro.app"))
}
