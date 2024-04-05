import build.api.dsl.*
import build.api.platform.Os
import build.api.platform.OsArch
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

application {
	applicationName = Build.APP_NAME
	mainClass.set("main.MainKt")
}

packaged {
	appNs = "$group.app"

	appTitle = extra["kokoro.app.title"] as String
	appTitleShort = extra["kokoro.app.title.short"] as String

	packageVersionCode = projectThis.getVersionBaseName()
	description = "${appTitle.get()} for desktop"
	vendor = "SRS Kokoro Project & N.N."
	copyright = "© 2021-2023, ${vendor.get()}"

	licenseFile = rootProject.file("LICENSE.txt", PathValidation.FILE)
}

// --

val flatlaf_natives: Configuration by configurations.creating

distributions {
	appHome {
		val contents = contents

		contents.from(packaged.licenseFile) {
			into("legal")
		}

		contents.from(flatlaf_natives) {
			into("flatlaf")

			eachFile(fun(e): Unit = e.name.run {
				if (startsWith("flatlaf-", ignoreCase = true)) {
					val components = split('-')
					check(components.size == 4)

					val (_, _, os, archExt) = components
					e.name = "flatlaf-$os-$archExt"
				}
			})
		}
	}
}

tasks.shadowJar {
	archiveFileName = Build.APP_SHADOW_JAR
	// KLUDGE for https://github.com/johnrengelman/shadow/issues/449
	manifest.attributes["Multi-Release"] = true
}

tasks.startShadowScripts {
	// Prefer the "start scripts" template provided by Gradle.
	val startScripts = project.tasks.startScripts.get()
	unixStartScriptGenerator.cast<TemplateBasedScriptGenerator>().template =
		startScripts.unixStartScriptGenerator.cast<TemplateBasedScriptGenerator>().template
	windowsStartScriptGenerator.cast<TemplateBasedScriptGenerator>().template =
		startScripts.windowsStartScriptGenerator.cast<TemplateBasedScriptGenerator>().template
	mainClass = startScripts.mainClass
}

// --

private object Utils {
	fun E_UnsupportedOsAndArch(os: Os, osArch: OsArch) =
		IllegalStateException("Unsupported OS + Arch combination: $os + $osArch")
}

dependencies {
	deps {
		wixBinZipUrl = prop("build.wixBinZipUrl")
	}

	val os = Os.current
	val osArch = OsArch.current

	// See, "Native Libraries distribution | FlatLaf - Flat Look and Feel" --
	// https://www.formdev.com/flatlaf/native-libraries/#gradle_no_natives_jar
	flatlaf_natives("com.formdev:flatlaf::" + when (os) {
		Os.WINDOWS -> when (osArch) {
			OsArch.X86 -> "windows-x86@dll"
			OsArch.X86_64 -> "windows-x86_64@dll"
			OsArch.AARCH64 -> "windows-arm64@dll"
		}
		Os.MACOS -> when (osArch) {
			OsArch.X86_64 -> "macos-x86_64@dylib"
			OsArch.AARCH64 -> "macos-arm64@dylib"
			else -> throw Utils.E_UnsupportedOsAndArch(os, osArch)
		}
		Os.LINUX -> when (osArch) {
			OsArch.X86_64 -> "linux-x86_64@so"
			else -> throw Utils.E_UnsupportedOsAndArch(os, osArch)
		}
	})

	implementation(project(":kokoro.app"))
}
