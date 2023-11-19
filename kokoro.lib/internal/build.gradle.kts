plugins {
	id("kokoro.conv.kt.mpp.lib")
	id("conv.kt.mpp.targets")
	id("conv.gmazzo.buildconfig")
	id("conv.version")
}

base.archivesName.set("kokoro-internal")

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

val NAMESPACE = extra["kokoro.internal.ns"] as String

android {
	namespace = NAMESPACE
}

buildConfig {
	publicTopLevel() inPackage NAMESPACE

	val isReleasing = isReleasing
	require(isDebug == !isReleasing) {
		throw AssertionError("Required: `${::isDebug.name} == !${::isReleasing.name}`")
	}

	buildConfigField("boolean", "IS_RELEASING", "$isReleasing")
	buildConfigField("boolean", "RELEASE", "IS_RELEASING")
	buildConfigField("boolean", "DEBUG", "!RELEASE")
}

dependencies {
	appMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	appMainImplementation("com.squareup.okio:okio")
}
