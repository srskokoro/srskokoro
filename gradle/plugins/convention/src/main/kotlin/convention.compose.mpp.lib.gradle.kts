import convention.*

plugins {
	id("convention.kotlin.mpp.lib")
	id("org.jetbrains.compose")
}

dependencies {
	setUpComposeDeps {
		commonMainImplementation(it)
	}
	setUpComposePreviewDeps {
		// Needed only for JB compose `@Preview` annotation
		commonMainImplementation(it)
		// TODO ^ Not really supported (at least currently) in JS.
		// - A potential workaround is to create a dummy `@Preview` annotation
		// for JS, and use a `typealias` instead when in JVM or Android.
	}
}
