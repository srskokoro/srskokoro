plugins {
	`kotlin-dsl`
}

dependencies {
	compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin")
	implementation("convention:build-support")
	implementation(project(":convention"))

	implementation("app.cash.redwood:redwood-gradle-plugin")
	implementation("com.google.javascript:closure-compiler")
}
