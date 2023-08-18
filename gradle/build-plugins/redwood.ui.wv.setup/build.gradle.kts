plugins {
	id("conv.kt.dsl")
}

dependencies {
	compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin")
	implementation("convention:build-support")
	implementation(project(":conventions"))

	implementation("app.cash.redwood:redwood-gradle-plugin")
	implementation("com.google.javascript:closure-compiler")
}
