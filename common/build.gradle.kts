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
		// TODO Uncomment eventually to allow `.java` sources -- https://youtrack.jetbrains.com/issue/KT-30878
		//withJava()
		testRuns["test"].executionTask.configure {
			useJUnitPlatform()
		}
	}
}

android {
	namespace = "$group.common"
	compileSdk = 33

	defaultConfig {
		minSdk = 21
	}

	compileOptions {
		cfgs.jvm.verObj.let {
			sourceCompatibility = it
			targetCompatibility = it
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
