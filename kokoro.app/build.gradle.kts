plugins {
	id("kokoro.conv.kt.mpp.lib")
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
	deps.bundles.testExtras {
		commonTestImplementation(it)
	}

	commonMainImplementation(project(":kokoro.lib.internal"))

	desktopMainImplementation(jcef.dependency)
	androidMainApi("androidx.core:core-ktx:1.9.0")
	androidMainApi("androidx.activity:activity-ktx:1.6.1")

	commonMainImplementation("com.squareup.okio:okio:3.3.0")

	// https://github.com/harawata/appdirs
	desktopMainImplementation("net.harawata:appdirs:1.2.1")
}
