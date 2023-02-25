import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
	id("convention.compose.jvm.app")
	id("jcef-bundler")
}

val appResDirName = "res"
val appResDir = file(appResDirName)

jcef {
	installDirRel.set("jcef")
	dependsOnInstallTask<Sync>("prepareAppResources") {
		from(this@jcef.outputDir)
	}
}

compose.desktop.application {
	mainClass = "MainKt"
	jvmArgs += jcef.recommendedJvmArgs

	nativeDistributions {
		appResourcesRootDir.set(appResDir)
		targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

		packageName = "SRS Kokoro"
		packageVersion = "1.0.0"

		description = "$packageName Desktop App"
		vendor = "SRS Kokoro Project & N.N."
		copyright = "Copyright (C) 2022 $vendor"
		licenseFile.set(rootProject.file("LICENSE.txt", PathValidation.FILE).absoluteFile)

		linux {
			menuGroup = packageName
			shortcut = true
		}
		macOS {
			dockName = packageName
			bundleID = "$group.app"
		}
		windows {
			menuGroup = packageName
			upgradeUuid = "BAF69324-95CC-4FC4-B156-267ACA640116"
			dirChooser = true
			shortcut = true
		}
	}
}

dependencies {
	deps.bundles.testExtras {
		testImplementation(it)
	}
	implementation(project(":app"))
	implementation(jcef.dependency)
}
