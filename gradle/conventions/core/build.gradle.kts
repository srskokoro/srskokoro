plugins {
	id("build.conventions")
}

dependencies {
	implementation("build.foundation:complement")
	implementation("build.foundation:dependencies")

	implementation(plugin("de.undercouch.download"))
	implementation(plugin("com.github.johnrengelman.shadow"))

	api(project(":support.kotlin-gradle-plugin"))
	implementation(kotlin("sam-with-receiver"))
	implementation(kotlin("assignment"))

	implementation("org.jetbrains.kotlinx:atomicfu-gradle-plugin") {
		// Needed because Gradle API already has an SLF4J binding and having
		// more than one causes a warning to be reported (particularly, in unit
		// tests). See, http://www.slf4j.org/codes.html#multiple_bindings
		exclude("org.slf4j", "slf4j-simple")
	}
	implementation("com.android.tools.build:gradle")
	implementation(plugin("io.kotest.multiplatform"))
	implementation(plugin("com.github.gmazzo.buildconfig"))

	testImplementation(project(":testing.conventions"))
}
