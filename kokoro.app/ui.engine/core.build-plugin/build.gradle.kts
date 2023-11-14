plugins {
	id("conv.plugins.kt.dsl")
}

dependencies {
	compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin")

	implementation("convention:build-support")
	implementation("convention:conventions")

	implementation("com.google.javascript:closure-compiler")
}
