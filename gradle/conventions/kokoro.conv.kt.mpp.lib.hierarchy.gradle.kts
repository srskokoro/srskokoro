import conv.util.*

plugins {
	id("conv.kt.mpp.lib")
}

kotlin {
	val targetsExtensions = targets.extensions

	androidTarget("android") asExtensionIn targetsExtensions
	jvm("desktopJvm") asExtensionIn targetsExtensions

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

		val (appMain, appTest) = derive("app", commonMain, commonTest)

		val (desktopMain, desktopTest) = derive("desktop", appMain, appTest)
		val (mobileMain, mobileTest) = derive("mobile", appMain, appTest)

		// --

		val (jvmishMain, jvmishTest) = derive("jvmish", appMain, appTest)
		val (nativeMain, nativeTest) = derive("native", appMain, appTest)

		val (unixMain, unixTest) = derive("unix", nativeMain, nativeTest)
		val (appleMain, appleTest) = derive("apple", unixMain, unixTest)

		val (iosMain, iosTest) = derive("ios", appleMain, appleTest)

		// --

		val androidMain/**/ by getting { dependsOn(jvmishMain); dependsOn(mobileMain) }
		val androidUnitTest by getting { dependsOn(jvmishTest); dependsOn(mobileTest) }

		val desktopJvmMain by getting { dependsOn(jvmishMain); dependsOn(desktopMain) }
		val desktopJvmTest by getting { dependsOn(jvmishTest); dependsOn(desktopTest) }

		with(iosMain) { dependsOn(mobileMain) }
		with(iosTest) { dependsOn(mobileTest) }

		val iosX64Main by getting { dependsOn(iosMain) }
		val iosX64Test by getting { dependsOn(iosTest) }

		val iosArm64Main by getting { dependsOn(iosMain) }
		val iosArm64Test by getting { dependsOn(iosTest) }

		val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
		val iosSimulatorArm64Test by getting { dependsOn(iosTest) }
	}
}
