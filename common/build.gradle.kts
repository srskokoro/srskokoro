plugins {
	id("convention.kotlin-multiplatform.lib")
	id("io.kotest.multiplatform")
	id("org.jetbrains.compose")
	id("jcef-bundler")
}

kotlin {
	android()
	jvm("desktop") {
		// TODO Uncomment eventually to allow `.java` sources -- https://youtrack.jetbrains.com/issue/KT-30878
		//withJava()
	}
}

android {
	namespace = "$group.common"
	compileSdk = 33

	defaultConfig {
		minSdk = 21
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
