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

// Cache as it seems costly to obtain each time
internal val kotlinSourceSets = localKotlin.sourceSets

localKotlin.targets.apply {
	// Nothing
}.onType(KotlinAndroidTarget::class) {
	kotlinSourceSets.let {
		it.findByName("${name}UnitTest") ?: it["${name}Test"]
	}.dependencies {
		setUpTestFrameworkDeps_android {
			implementation(it)
		}
	}
}.onType(KotlinJvmTarget::class) {
	testRuns["test"].executionTask.configure {
		setUp(this)
	}
	kotlinSourceSets["${name}Test"].dependencies {
		setUpTestFrameworkDeps_jvm {
			implementation(it)
		}
	}
}
kotlinSourceSets.commonTest {
	dependencies {
		setUpTestFrameworkDeps_kmp_common {
			implementation(it)
		}
		setUpTestCommonDeps {
			implementation(it)
		}
	}
}
