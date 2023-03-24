plugins {
	/*
	 * NOTE: While we could have a single "compose multiplatform" project,
	 * without the multiplatform project being a library, we might face some
	 * issues that we would rather not deal with.
	 * - See, for example, https://github.com/JetBrains/compose-jb/issues/2345
	 * - Nonetheless, should doing so make things more maintainable, dealing
	 * with any issues introduced might be worth it.
	 */
	id("convention.kotlin.mpp.lib")
	id("jcef-bundler-dependency")
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

	targets.desktop {
		// TODO Uncomment eventually to allow `.java` sources -- https://youtrack.jetbrains.com/issue/KT-30878
		//withJava()
	}
}

android {
	namespace = extra["srs.kokoro.app.ns"] as String
	compileSdk = 33

	defaultConfig {
		minSdk = 21
	}
}

dependencies {
	deps.bundles.testExtras {
		commonTestImplementation(it)
	}

	desktopMainImplementation(jcef.dependency)
	androidMainApi("androidx.core:core-ktx:1.9.0")
	androidMainApi("androidx.activity:activity-ktx:1.6.1")
}
