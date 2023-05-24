plugins {
	id("kokoro.conv.kt.mpp.lib")
	id("conv.gmazzo.buildconfig")
	id("conv.version")
}

kotestConfigClass = "KotestConfig"

kotlin {
	/**
	 * See 'build.targets.txt' to declare build targets, then use
	 * `targets.<targetName>` here to configure them further.
	 *
	 * See also [conv.internal.setup.setUpTargetsExtensions] in the convention
	 * plugins.
	 */
	@Suppress("UNUSED_VARIABLE") val eat_comment: Nothing

	targets.desktop {
		// TODO Uncomment eventually to allow `.java` sources -- https://youtrack.jetbrains.com/issue/KT-30878
		//withJava()
	}
}

val NAMESPACE = extra["kokoro.internal.ns"] as String

android {
	namespace = NAMESPACE
}

buildConfig {
	publicTopLevel() inPackage NAMESPACE
	val isReleasing = isReleasing
	buildConfigField("boolean", "IS_RELEASING", "$isReleasing")
	val isDebug = isDebug
	require(isDebug == !isReleasing) { throw AssertionError() }
	buildConfigField("boolean", "DEBUG", "$isDebug")
}

dependencies {
	deps.bundles.testExtras *= {
		commonTestImplementation(it)
	}
}
