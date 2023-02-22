import convention.*
import convention.util.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
	id("convention.base")
	kotlin("multiplatform")
	id("io.kotest.multiplatform")
}

withAndroid {
	setUp(this)
}

internal val localKotlin = kotlin.apply {
	setUp(this)
	setUpTargetsViaConfig(this)
}

localKotlin.targets.apply {
	// Nothing
}.onType(KotlinAndroidTarget::class) {
	unitTestSourceSet.dependencies {
		setUpTestFrameworkDeps_android {
			implementation(it)
		}
	}
}.onType(KotlinJvmTarget::class) {
	testRuns["test"].executionTask.configure {
		setUp(this)
	}
	testSourceSet.dependencies {
		setUpTestFrameworkDeps_jvm {
			implementation(it)
		}
	}
}
dependencies {
	fun implementation(dep: String) = run {
		commonTestImplementation(dep)
	}
	setUpTestFrameworkDeps_kmp_common {
		implementation(it)
	}
	setUpTestCommonDeps {
		implementation(it)
	}
}
