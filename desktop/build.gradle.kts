import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
	kotlin("jvm")
	id("org.jetbrains.compose")
	id("build-support")
}

val javaToolchainHome = javaToolchains.launcherFor(cfgs.jvm.toolchainConfig)
	.map { it.metadata.installationPath.asFile.absolutePath }

kotlin {
	jvmToolchain(cfgs.jvm.toolchainConfig)

	target {
		compilations.all {
			// TODO Remove eventually -- See, https://github.com/JetBrains/compose-jb/issues/2511
			kotlinOptions.jvmTarget = cfgs.jvm.kotlinOptTarget
		}
	}
}
tasks.test {
	useJUnitPlatform()
}

compose.desktop {
	application {
		mainClass = "MainKt"
		// TODO Remove eventually -- See, https://github.com/JetBrains/compose-jb/pull/2515
		javaHome = javaToolchainHome.get()

		nativeDistributions {
			targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

			packageName = "SRS Kokoro"
			packageVersion = "1.0.0"

			description = "$packageName Desktop App"
			vendor = "SRS Kokoro Project & N.N."
			copyright = "Copyright (C) 2022 $vendor"

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

// TEST dependencies ONLY
dependencies {
	testImplementation(libs.kotest.runner.junit5)
	testImplementation(libs.bundles.test.common)
}

// MAIN dependencies
dependencies {
	implementation(compose.desktop.currentOs)
	implementation(project(":common"))
}
