plugins {
	id("kokoro.conv.kt.mpp.lib")
	id("conv.gmazzo.buildconfig")
	id("conv.version")
	id("jcef-bundler-dependency")
}

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

val NAMESPACE = extra["kokoro.app.ns"] as String

android {
	namespace = NAMESPACE
}

buildConfig.appMain {
	internalObject("AppBuild") inPackage NAMESPACE
	buildConfigField("String", "VERSION", "\"$version\"")
	buildConfigField("int", "VERSION_CODE", "$versionCode")
	if (versionCode == 0) throw InvalidUserDataException(
		"Version code 0 (zero) should not be used"
	)
}

buildConfig.desktopMain {
	internalObject("AppBuildDesktop") inPackage NAMESPACE
	buildConfigField("String", "APP_DATA_DIR_NAME", "\"SRSKokoro${if (isReleasing) "" else "-Dev"}\"")
}

dependencies {
	deps.bundles.testExtras *= {
		commonTestImplementation(it)
	}

	commonMainImplementation(project(":kokoro.lib.internal"))
	appMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	desktopMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing")

	desktopMainImplementation(jcef.dependency)
	androidMainApi("androidx.core:core-ktx")
	androidMainApi("androidx.activity:activity-ktx")

	appMainImplementation("com.squareup.okio:okio")
	desktopMainImplementation("net.harawata:appdirs")

	desktopMainImplementation("com.github.ajalt.clikt:clikt")

	desktopMainImplementation("com.formdev:flatlaf")
	// See, https://www.formdev.com/flatlaf/native-libraries/
	// TODO Auto-detect which native library to use for current OS
	desktopMainImplementation("com.formdev:flatlaf::linux-x86_64@so")
	desktopMainImplementation("com.formdev:flatlaf::windows-x86_64@dll")
	desktopMainImplementation("com.formdev:flatlaf::windows-x86@dll")
}
