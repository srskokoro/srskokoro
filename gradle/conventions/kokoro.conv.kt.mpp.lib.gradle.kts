import convention.util.*

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

		val jvmCommonMain by creating { dependsOn(commonMain) }
		val jvmCommonTest by creating { dependsOn(commonTest) }

		val androidMain/**/ by getting { dependsOn(jvmCommonMain) }
		val androidUnitTest by getting { dependsOn(jvmCommonTest) }

		val desktopMain by getting { dependsOn(jvmCommonMain) }
		val desktopTest by getting { dependsOn(jvmCommonTest) }
	}
}
