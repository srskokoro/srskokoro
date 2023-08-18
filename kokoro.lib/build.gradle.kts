plugins {
	id("kokoro.conv.kt.mpp.lib")
	id("conv.kt.mpp.targets")
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

	targets.desktopJvm {
		// TODO Uncomment eventually to allow `.java` sources -- https://youtrack.jetbrains.com/issue/KT-30878
		//withJava()
	}
}

val NAMESPACE = extra["kokoro.ns"] as String

android {
	namespace = NAMESPACE
}

dependencies {
	deps.bundles.testExtras *= {
		commonTestImplementation(it)
	}
	commonTestImplementation(project(":kokoro.lib.test.support"))

	commonMainImplementation(project(":kokoro.lib.internal"))
}
