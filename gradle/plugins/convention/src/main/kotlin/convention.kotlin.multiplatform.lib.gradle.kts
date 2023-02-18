import convention.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget

plugins {
	id("com.android.library")
	id("convention.kotlin.multiplatform.base")
}

android {
	setUp(this)
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
