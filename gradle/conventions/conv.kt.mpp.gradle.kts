import conv.internal.setup.*
import conv.internal.util.*
import conv.util.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
	id("conv.base")
	kotlin("multiplatform")
	id("io.kotest.multiplatform")
}

withAndroid {
	setUp(this)
}

internal val localKotlin = kotlin.apply {
	setUp(this)
}

tasks.withType<KotlinJvmCompile>().configureEach {
	setUp(compilerOptions)
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
	// https://kotlinlang.org/docs/gradle-configure-project.html#versions-alignment-of-transitive-dependencies
	commonMainImplementation(platform("org.jetbrains.kotlin:kotlin-bom"))

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
