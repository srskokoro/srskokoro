plugins {
	id("conv.plugins.kt.dsl")
}

dependencies {
	compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin")

	implementation("build:build.support")
	implementation("build:conventions")

	implementation("com.google.javascript:closure-compiler")
}
