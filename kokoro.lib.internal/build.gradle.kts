import convention.util.*

plugins {
	id("convention.kotlin.mpp.lib")
}

kotlin {
	/**
	 * See 'build.targets.txt' to declare build targets, then use
	 * `targets.<targetName>` here to configure them further.
	 *
	 * See also [convention.internal.setup.setUpTargetsViaConfig] in the
	 * convention plugins.
	 */
	@Suppress("UNUSED_VARIABLE") val eat_comment: Nothing

	sourceSets {
		val commonSs = obtain(commonMain, commonTest)
		val jvmCommonSs = obtain(jvmCommonMain, jvmCommonTest)
			.also { it dependsOn commonSs }

		obtain(androidMain, androidUnitTest) dependsOn jvmCommonSs
		obtain(desktopMain, desktopTest) dependsOn jvmCommonSs
	}
}

android {
	namespace = extra["srs.kokoro.internal.ns"] as String
}

dependencies {
	deps.bundles.testExtras {
		commonTestImplementation(it)
	}
}
