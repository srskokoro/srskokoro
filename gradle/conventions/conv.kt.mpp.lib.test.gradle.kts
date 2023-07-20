import conv.internal.setup.*
import conv.internal.util.*
import conv.util.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
	id("conv.kt.mpp.lib")
}

kotlin.targets.apply {
	// Nothing
}.onType(KotlinAndroidTarget::class) {
	mainSourceSet.dependencies {
		setUpTestFrameworkDeps_android {
			implementation(it)
		}
	}
}.onType(KotlinJvmTarget::class) {
	mainSourceSet.dependencies {
		setUpTestFrameworkDeps_jvm {
			implementation(it)
		}
	}
}
dependencies {
	fun implementation(dep: String) = run {
		commonMainImplementation(dep)
	}
	setUpTestFrameworkDeps_kmp_common {
		implementation(it)
	}
	setUpTestCommonDeps {
		implementation(it)
	}
}
