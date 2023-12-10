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

// --

val AGP_COMPILE_ONLY_VERSION = "8.1.4"

val KOTEST_VERSION = "5.8.0"
val ASSERTK_VERSION = "0.28.0"

fun buildConfigData() = """
const val ${::KOTEST_VERSION.name} = "$KOTEST_VERSION"
const val ${::ASSERTK_VERSION.name} = "$ASSERTK_VERSION"
""".trimIndent()

kotlin.sourceSets.main {
	kotlin.srcDir(tasks.register("config") {
		val outputDir = project.layout.buildDirectory.dir("generated/$name")
		outputs.dir(outputDir)
		doFirst {
			File(outputDir.get().asFile, "config.kt").outputStream()
				.use { it.write(buildConfigData().encodeToByteArray()) }
		}
	})
}

dependencies {
	implementation(kotlin("gradle-plugin"))
	implementation("org.gradle.kotlin", "gradle-kotlin-dsl-plugins", expectedKotlinDslPluginsVersion)

	implementation(platform("io.kotest:kotest-bom:$KOTEST_VERSION"))
	implementation("io.kotest:kotest-framework-api") // So that we get access to, e.g., `io.kotest.core.internal.KotestEngineProperties`
	implementation("io.kotest:kotest-framework-multiplatform-plugin-gradle")

	compileOnly("com.android.tools.build", "gradle", AGP_COMPILE_ONLY_VERSION)
}
