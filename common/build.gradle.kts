import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
	id("com.android.library")
	kotlin("multiplatform")
	id("io.kotest.multiplatform")
	id("org.jetbrains.compose")
	id("build-support")
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

val localKotlin = kotlin
val localCompose = localKotlin.compose

fun sourceSet(name: String) = localKotlin.sourceSets.named(name)

@Suppress("unused")
inline val KotlinDependencyHandler.compose
	get() = localCompose

infix fun NamedDomainObjectProvider<KotlinSourceSet>.dependencies(
	configure: KotlinDependencyHandler.() -> Unit
) = configure { dependencies(configure) }

// --=--
// TEST dependencies ONLY

sourceSet("commonTest") dependencies {
	implementation(libs.kotest.framework.engine)
	implementation(libs.bundles.test.common)
}

sourceSet("desktopTest") dependencies {
	implementation(libs.kotest.runner.junit5)
}

// --=--
// MAIN dependencies

sourceSet("commonMain") dependencies {
	api(compose.runtime)
	api(compose.foundation)
	api(compose.material)
	// Needed only for preview.
	implementation(compose.preview)
}

sourceSet("androidMain") dependencies {
	api("androidx.core:core-ktx:1.9.0")
}
