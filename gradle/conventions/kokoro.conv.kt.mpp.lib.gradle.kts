import conv.util.*

plugins {
	id("conv.kt.mpp.lib")
}

kotlin {
	val targetsExtensions = targets.extensions

	android() asExtensionIn targetsExtensions
	jvm("desktop") asExtensionIn targetsExtensions

	sourceSets @Suppress("UNUSED_VARIABLE") {
		val commonMain by getting
		val commonTest by getting

		val jvmishMain by creating { dependsOn(commonMain) }
		val jvmishTest by creating { dependsOn(commonTest) }

		val androidMain/**/ by getting { dependsOn(jvmishMain) }
		val androidUnitTest by getting { dependsOn(jvmishTest) }

		val desktopMain by getting { dependsOn(jvmishMain) }
		val desktopTest by getting { dependsOn(jvmishTest) }
	}
}
