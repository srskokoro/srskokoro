plugins {
	id("convention.kotlin-multiplatform.lib")
	id("io.kotest.multiplatform")
	id("org.jetbrains.compose")
	id("jcef-bundler")
}

kotlin {
	jvmToolchain(deps.jvm.toolchainConfig)

	android()

	jvm("desktop") {
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
		// TODO Remove eventually -- See, https://issuetracker.google.com/issues/260059413
		deps.jvm.verObj.let {
			sourceCompatibility = it
			targetCompatibility = it
		}
	}
}

@Suppress("UnstableApiUsage")
dependencies {
	desktopTestImplementation("io.kotest:kotest-runner-junit5")
	commonTestImplementation("io.kotest:kotest-framework-engine")
	deps.bundles.testCommon {
		commonTestImplementation(it)
	}

	commonMainApi(compose.runtime)
	commonMainApi(compose.foundation)
	commonMainApi(compose.material)
	// Needed only for preview.
	commonMainImplementation(compose.preview)

	desktopMainImplementation(jcef.dependency)
	androidMainApi("androidx.core:core-ktx:1.9.0")
}
