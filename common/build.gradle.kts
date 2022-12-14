plugins {
	id("com.android.library")
	kotlin("multiplatform")
	id("io.kotest.multiplatform")
	id("org.jetbrains.compose")
	id("jcef-bundler")
	id("convention--kotlin-multiplatform")
}

kotlin {
	jvmToolchain(cfgs.jvm.toolchainConfig)

	android {
		compilations.all {
			// See, https://stackoverflow.com/a/67024907
			kotlinOptions.jvmTarget = cfgs.jvm.kotlinOptTarget
		}
	}
	jvm("desktop") {
		compilations.all {
			// TODO Remove eventually -- See, https://github.com/JetBrains/compose-jb/issues/2511
			kotlinOptions.jvmTarget = cfgs.jvm.kotlinOptTarget
		}
		testRuns["test"].executionTask.configure {
			useJUnitPlatform()
		}
	}

	@Suppress("UNUSED_VARIABLE") // --
	sourceSets {
		// Remove log pollution until Android support in KMP improves
		// - See, https://discuss.kotlinlang.org/t/21448
		run {
			val androidTestFixtures by getting

			val androidTestFixturesDebug by getting { dependsOn(androidTestFixtures) }
			val androidAndroidTestDebug by getting { dependsOn(androidTestFixturesDebug) }

			val androidTestFixturesRelease by getting { dependsOn(androidTestFixtures) }
			val androidAndroidTestRelease by getting { dependsOn(androidTestFixturesRelease) }
			val androidTestRelease by getting { dependsOn(androidAndroidTestRelease) }
		}
	}
}

android {
	namespace = "$group.common"
	compileSdk = 33

	defaultConfig {
		minSdk = 21
		targetSdk = 33
	}

	compileOptions {
		cfgs.jvm.verObj.let {
			sourceCompatibility = it
			targetCompatibility = it
		}
	}

	sourceSets {
		named("main") {
			manifest.srcFile("src/androidMain/AndroidManifest.xml")
			res.srcDirs("src/androidMain/res")
		}
	}
}

@Suppress("UnstableApiUsage")
dependencies {
	desktopTestImplementation(libs.kotest.runner.junit5)
	commonTestImplementation(libs.kotest.framework.engine)
	commonTestImplementation(libs.bundles.test.common)

	commonMainApi(compose.runtime)
	commonMainApi(compose.foundation)
	commonMainApi(compose.material)
	// Needed only for preview.
	commonMainImplementation(compose.preview)

	desktopMainImplementation(jcef.dependency)
	androidMainApi("androidx.core:core-ktx:1.9.0")
}
