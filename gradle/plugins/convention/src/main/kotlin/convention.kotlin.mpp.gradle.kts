import convention.*
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
internal val kotlinTargets = localKotlin.targets

kotlinTargets.withType<KotlinAndroidTarget> {
	with(kotlinSourceSets) {
		findByName("${name}UnitTest") ?: get("${name}Test")
	}.dependencies {
		setUpTestFrameworkDeps_android {
			implementation(it)
		}
	}
}
kotlinTargets.withType<KotlinJvmTarget> {
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
