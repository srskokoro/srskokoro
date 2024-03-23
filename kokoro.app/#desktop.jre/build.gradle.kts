import build.api.dsl.*
import build.support.cast

plugins {
	id("build.kt.jvm.app.packaged")
	id("build.version")
}

group = evaluatedParent.group

private object Build {
	const val APP_NAME = "kokoro-app"
	const val APP_SHADOW_JAR = "$APP_NAME.jar"
}

packaged {
	bundleName = Build.APP_NAME
	appNs = "$group.app"

	appTitle = extra["kokoro.app.title"] as String
	appTitleShort = extra["kokoro.app.title.short"] as String

	packageVersionCode = projectThis.getVersionBaseName()
	description = "${appTitle.get()} for desktop"
	vendor = "SRS Kokoro Project & N.N."
	copyright = "© 2021-2023, ${vendor.get()}"

	licenseFile = rootProject.file("LICENSE.txt", PathValidation.FILE)
}

application {
	mainClass.set("main.MainKt")
	applicationName = Build.APP_NAME
}

tasks.shadowJar {
	archiveFileName = Build.APP_SHADOW_JAR
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
