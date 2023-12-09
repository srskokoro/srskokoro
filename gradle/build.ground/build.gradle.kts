import org.gradle.kotlin.dsl.support.expectedKotlinDslPluginsVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
	val KOTLIN_JVM_TARGET = JvmTarget.JVM_1_8
	const val JAVAC_RELEASE_OPT = "--release=8"
}
with(Build) {
	tasks.withType<KotlinCompile>().configureEach {
		jvmTargetValidationMode.set(JvmTargetValidationMode.IGNORE)
		compilerOptions.jvmTarget.set(KOTLIN_JVM_TARGET)
	}
	tasks.withType<JavaCompile>().configureEach {
		options.compilerArgs.add(JAVAC_RELEASE_OPT)
	}
}

dependencies {
	compileOnly(gradleApi())
	compileOnly(gradleKotlinDsl())

	implementation(kotlin("gradle-plugin"))
	implementation("org.gradle.kotlin", "gradle-kotlin-dsl-plugins", expectedKotlinDslPluginsVersion)
}
