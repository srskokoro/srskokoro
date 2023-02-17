import convention.setUpConvention
import convention.setUpTestFrameworkDeps_android
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget

plugins {
	id("com.android.library")
	id("convention.kotlin-multiplatform.base")
}

android {
	setUpConvention()
}

kotlin {
	targets.withType<KotlinAndroidTarget> {
		(sourceSets.findByName("${name}UnitTest") ?: sourceSets["${name}Test"]).dependencies {
			setUpTestFrameworkDeps_android {
				implementation(it)
			}
		}
	}
}
