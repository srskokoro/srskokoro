plugins {
	id("conv.kt.dsl")
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

	implementation("convention:build-support")
	implementation("convention:settings")
	implementation(project(":conventions"))

	implementation("com.google.javascript:closure-compiler")
}
