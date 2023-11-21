plugins {
	id("conv.plugins.kt.dsl")
}

gradlePlugin {
	plugins {
		register("wvSetup") {
			id = "kokoro.app.ui.wv.setup"
			implementationClass = "kokoro.app.ui.wv.setup.WvSetupPlugin"
		}
	}
}

dependencies {
	compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin")

	implementation("build:build.support")
	implementation("build:settings")
	implementation("convention:conventions")

	implementation("com.google.javascript:closure-compiler")
}
