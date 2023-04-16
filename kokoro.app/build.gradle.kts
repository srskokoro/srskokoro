plugins {
	id("kokoro.conv.kt.mpp.lib")
	id("conv.gmazzo.buildconfig")
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

android {
	namespace = extra["srs.kokoro.app.ns"] as String
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
}
