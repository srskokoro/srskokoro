plugins {
	id("convention.compose.mpp.lib")
	id("jcef-bundler")
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
	namespace = "$group.common"
	compileSdk = 33

	defaultConfig {
		minSdk = 21
	}
}

@Suppress("UnstableApiUsage")
dependencies {
	deps.bundles.testExtras {
		commonTestImplementation(it)
	}

	desktopMainImplementation(jcef.dependency)
	androidMainApi("androidx.core:core-ktx:1.9.0")

	// Needed only for JB compose `@Preview` annotation
	compose.preview.let {
		androidMainApi(it)
		desktopMainApi(it)
	}
}
