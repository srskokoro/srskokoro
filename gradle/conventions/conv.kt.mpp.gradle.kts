import conv.internal.setup.*
import conv.internal.util.*
import conv.util.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
	id("conv.base")
	kotlin("multiplatform")
	id("io.kotest.multiplatform")
}

withAndroid {
	setUp(this)
}

kotlin.apply {
	setUp(this)
}.targets.apply {
	// Nothing (for now)
}.all(@Suppress("CascadeIf") fun(t) = t.apply {
	// Nothing (for now)
}.let(fun(t) = if (t is KotlinAndroidTarget) t.run {
	unitTestSourceSet.dependencies {
		setUpTestFrameworkDeps_android {
			implementation(it)
		}
	}
	compilations.all {
		setUpKotlinVersions(this)
		setUp(compilerOptions.options)
	}
} else if (t is KotlinJvmTarget) t.run {
	testRuns["test"].executionTask.configure {
		setUp(this)
	}
	testSourceSet.dependencies {
		setUpTestFrameworkDeps_jvm {
			implementation(it)
		}
	}
	compilations.all {
		setUpKotlinVersions(this)
		setUp(compilerOptions.options)
	}
} else t.run {
	compilations.all {
		setUpKotlinVersions(this)
	}
}))
dependencies {
	// https://kotlinlang.org/docs/gradle-configure-project.html#versions-alignment-of-transitive-dependencies
	platform("org.jetbrains.kotlin:kotlin-bom").let { bom ->
		commonMainImplementation(bom)
		commonTestImplementation(bom)
	}

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
