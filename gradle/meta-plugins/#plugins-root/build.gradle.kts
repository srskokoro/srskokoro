import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	`kotlin-dsl`
}

// NOTE: The following setup ensures that the sources of the current build are
// compiled to a low enough Java version compatible with Gradle and Kotlin. See,
// - https://docs.gradle.org/current/userguide/compatibility.html
// - https://kotlinlang.org/docs/faq.html#which-versions-of-jvm-does-kotlin-target
//
// This setup is important since the current build is expected to be evaluated
// prior to every other build in the project and we don't want it to fail.
//
object Build {
	val kotlinJvmTarget = JvmTarget.JVM_1_8
	const val javacReleaseOpt = "--release=8"
}
project.extra["kotlin.jvm.target.validation.mode"] = "ignore"
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
	compilerOptions.jvmTarget.set(Build.kotlinJvmTarget)
}
tasks.withType<JavaCompile>().configureEach {
	options.compilerArgs.add(Build.javacReleaseOpt)
}

dependencies {
	compileOnly("org.jetbrains.kotlin", "kotlin-gradle-plugin", embeddedKotlinVersion)
}
