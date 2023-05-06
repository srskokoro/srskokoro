import conv.util.*

plugins {
	id("conv.kt.mpp.lib")
}

kotlin {
	val targetsExtensions = targets.extensions

	android() asExtensionIn targetsExtensions
	jvm("desktop") asExtensionIn targetsExtensions

	KotlinTargets(
		objects,
		iosX64() asExtensionIn targetsExtensions,
		iosArm64() asExtensionIn targetsExtensions,
		iosSimulatorArm64() asExtensionIn targetsExtensions,
	).let {
		targetsExtensions.add(typeOf(), "ios", it)
	}

	sourceSets @Suppress("UNUSED_VARIABLE") {
		val commonMain by getting
		val commonTest by getting

		val appMain by creating { /*--------------*/; dependsOn(commonMain) }
		val appTest by creating { dependsOn(appMain); dependsOn(commonTest) }

		val jvmishMain by creating { /*-----------------*/; dependsOn(appMain) }
		val jvmishTest by creating { dependsOn(jvmishMain); dependsOn(appTest) }

		val androidMain/**/ by getting { dependsOn(jvmishMain) }
		val androidUnitTest by getting { dependsOn(jvmishTest) }

		val desktopMain by getting { dependsOn(jvmishMain) }
		val desktopTest by getting { dependsOn(jvmishTest) }

		val iosMain by creating { /*--------------*/; dependsOn(appMain) }
		val iosTest by creating { dependsOn(iosMain); dependsOn(appTest) }

		val iosX64Main by getting { dependsOn(iosMain) }
		val iosX64Test by getting { dependsOn(iosTest) }

		val iosArm64Main by getting { dependsOn(iosMain) }
		val iosArm64Test by getting { dependsOn(iosTest) }

		val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
		val iosSimulatorArm64Test by getting { dependsOn(iosTest) }
	}
}
