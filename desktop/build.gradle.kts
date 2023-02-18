import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
	id("convention.kotlin.jvm")
	id("org.jetbrains.compose")
	id("jcef-bundler")
}

val appResDirName = "res"
val appResDir = file(appResDirName)

jcef {
	installDirRel.set("$appResDirName/common/jcef")
	dependsOnInstallTask<Sync>("prepareAppResources") {
		from(this@jcef.outputDir)
	}
}

compose.desktop {
	application {
		mainClass = "MainKt"
		jvmArgs += jcef.recommendedJvmArgs
		// TODO Remove eventually. See also, https://github.com/JetBrains/compose-jb/pull/2515
		afterEvaluate {
			if (javaHome == System.getProperty("java.home")) {
				// Workaround as it seems that configuring the JVM toolchain
				// doesn't automatically set the `javaHome` here.
				@Suppress("UsePropertyAccessSyntax")
				val launcher = javaToolchains.launcherFor(java.toolchain).getOrNull()
				if (launcher != null) {
					javaHome = launcher.metadata.installationPath.asFile.absolutePath
				} else {
					logger.warn("Warning: JVM toolchain was not configured.")
				}
			} else {
				logger.quiet("Custom `javaHome` set for `compose.desktop.application`:")
				logger.quiet("  $javaHome")
			}
		}

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
}

dependencies {
	deps.bundles.testExtras {
		testImplementation(it)
	}
	implementation(compose.desktop.currentOs)
	implementation(project(":common"))
	implementation(jcef.dependency)
}
