import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
	kotlin("jvm")
	id("org.jetbrains.compose")
}

val kotlinOptJvmTarget: String by rootProject.extra
val javaToolchainConfig: Action<JavaToolchainSpec> by rootProject.extra
val javaToolchainHome = javaToolchains.launcherFor(javaToolchainConfig)
	.map { it.metadata.installationPath.asFile.absolutePath }

kotlin {
	jvmToolchain(javaToolchainConfig)

	target {
		compilations.all {
			// TODO Remove eventually -- See, https://github.com/JetBrains/compose-jb/issues/2511
			kotlinOptions.jvmTarget = kotlinOptJvmTarget
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
			packageName = "KotlinMultiplatformComposeDesktopApplication"
			packageVersion = "1.0.0"
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
