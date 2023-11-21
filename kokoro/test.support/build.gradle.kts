plugins {
	id("conv.kt.mpp.lib.test")
	id("kokoro.conv.kt.mpp.lib.hierarchy")
	id("conv.kt.mpp.targets")
}

kotestConfigClass = "KotestConfig"

kotlin {
	/**
	 * See 'build.targets.cf' to declare build targets, then use
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

val NAMESPACE = extra["kokoro.internal.test.support.ns"] as String

android {
	namespace = NAMESPACE
}

dependencies {
	commonMainApi("org.jetbrains.kotlinx:kotlinx-coroutines-test")
	commonMainApi("io.kotest:kotest-property")
}