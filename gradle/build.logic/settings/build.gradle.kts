plugins {
	id("build.plugins.kt.dsl")
}

dependencies {
	implementation(project(":build.support"))
	implementation(project(":dependencies"))
	implementation("org.gradle.toolchains:foojay-resolver:0.5.0") // https://github.com/gradle/foojay-toolchains
}
