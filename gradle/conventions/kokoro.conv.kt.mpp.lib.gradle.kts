import conv.util.*

plugins {
	id("conv.kt.mpp.lib")
	id("conv.kt.mpp.targets")
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

		val (appMain, appTest) = derive("app", commonMain, commonTest)
		val (jvmishMain, jvmishTest) = derive("jvmish", appMain, appTest)

		val androidMain/**/ by getting { dependsOn(jvmishMain) }
		val androidUnitTest by getting { dependsOn(jvmishTest) }

		val desktopMain by getting { dependsOn(jvmishMain) }
		val desktopTest by getting { dependsOn(jvmishTest) }

		val (iosMain, iosTest) = derive("ios", appMain, appTest)

		val iosX64Main by getting { dependsOn(iosMain) }
		val iosX64Test by getting { dependsOn(iosTest) }

		val iosArm64Main by getting { dependsOn(iosMain) }
		val iosArm64Test by getting { dependsOn(iosTest) }

		val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
		val iosSimulatorArm64Test by getting { dependsOn(iosTest) }
	}
}
