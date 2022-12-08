import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
	kotlin("multiplatform")
	id("org.jetbrains.compose")
}

val kotlinOptJvmTarget: String by rootProject.extra
val javaToolchainConfig: Action<JavaToolchainSpec> by rootProject.extra
val javaToolchainHome = javaToolchains.launcherFor(javaToolchainConfig)
	.map { it.metadata.installationPath.asFile.absolutePath }

kotlin {
	jvmToolchain(javaToolchainConfig)

	jvm {
		compilations.all {
			// TODO Remove eventually -- See, https://github.com/JetBrains/compose-jb/issues/2511
			kotlinOptions.jvmTarget = kotlinOptJvmTarget
		}
		withJava()
		testRuns["test"].executionTask.configure {
			useJUnitPlatform()
		}
	}

	// TEST source sets ONLY
	sourceSets {
		named("commonTest") {
			dependencies {
				implementation(libs.bundles.test.common)
			}
		}
		named("jvmTest") {
			dependencies {
				implementation(libs.kotest.runner.junit5)
			}
		}
	}

	// MAIN source sets
	sourceSets {
		named("jvmMain") {
			dependencies {
				implementation(compose.desktop.currentOs)
				implementation(project(":common"))
			}
		}
	}
}

compose.desktop {
	application {
		mainClass = "MainKt"
		// TODO Remove eventually -- See, https://github.com/JetBrains/compose-jb/pull/2515
		javaHome = javaToolchainHome.get()

		nativeDistributions {
			targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
			packageName = "KotlinMultiplatformComposeDesktopApplication"
			packageVersion = "1.0.0"
		}
	}
}
