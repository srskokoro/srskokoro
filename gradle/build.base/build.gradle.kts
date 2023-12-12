import org.gradle.kotlin.dsl.support.expectedKotlinDslPluginsVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayInputStream
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion as KotlinCompileVersion

plugins {
	`kotlin-dsl`
}

val BaseProps = loadBaseProps("../../dependencies/base.properties")

val KOTLIN_VERSION: String by BaseProps
val KOTLIN_LANGUAGE_VERSION: String by BaseProps

val AGP_VERSION: String by BaseProps

val KOTEST_VERSION: String by BaseProps
val ASSERTK_VERSION: String by BaseProps

dependencies {
	// NOTICE: Any project used as build logic (or as contributor of build
	// plugins) should not use a `kotlin("stdlib")` higher than `embeddedKotlin("stdlib")`,
	// since the final consumer of such projects, the Gradle build itself,
	// enforces "strictly" that the version of `kotlin("stdlib")` be `embeddedKotlinVersion`
	// always. However, gotchas such as that won't be detected by Gradle until
	// it consumes in its build logic such projects.
	//
	// It would be nice if we can detect that problem early on, such as on IDE
	// syncs or during unit tests (which doesn't necessarily perform a full
	// build). We also want unit tests to not accidentally succeed when its
	// referenced APIs will eventually become unknowns in the actual full build.
	//
	// Now, at the moment (at the current time of writing this note at least),
	// the "only" purpose of the current project (and current build) is to set
	// up some build logic -- I mean, look at our dependencies: very surely,
	// non-build projects won't be able to consume this project without also
	// taking in those dependencies. And that, we expect that all other
	// consumers of this project is also for setting up some build logic.
	// Therefore, we don't have to wait for Gradle to enforce the described
	// "strict" `embeddedKotlinVersion` said above: we can do it ourselves now,
	// very early on in the build sequence (instead of waiting for Gradle to do
	// it unreliably for us much later on in the build sequence).
	api(kotlin("stdlib", "$embeddedKotlinVersion!!"))

	// WARNING: Both `stdlib` and KGP must be consistent with `kotlin-dsl`
	// plugin used in the build (or more precisely, the `embedded-kotlin`
	// plugin used by `kotlin-dsl`).
	//
	// NOTE: The version for KGP can be higher, but only if the consuming
	// project doesn't have the `kotlin-dsl` (or `embedded-kotlin`) plugin
	// applied anywhere under that project's parent build.
	implementation(embeddedKotlin("gradle-plugin"))
	implementation("org.gradle.kotlin", "gradle-kotlin-dsl-plugins", expectedKotlinDslPluginsVersion)

	// --

	api(enforcedPlatform("io.kotest:kotest-bom:$KOTEST_VERSION"))
	implementation("io.kotest:kotest-framework-api") // So that we get access to, e.g., `io.kotest.core.internal.KotestEngineProperties`
	implementation("io.kotest:kotest-framework-multiplatform-plugin-gradle")

	compileOnly("com.android.tools.build", "gradle", AGP_VERSION)
}

// --

val KOTLIN_LANGUAGE_VERSION_USED_IN_COMPILE = KotlinCompileVersion.fromVersion(KOTLIN_LANGUAGE_VERSION)

// NOTE: The following is to ensure that the sources of the current build are
// compiled to a low enough Java version compatible with Gradle and Kotlin. See
// also, https://kotlinlang.org/docs/faq.html#which-versions-of-jvm-does-kotlin-target
//
// This is important since the current build is expected to be evaluated prior
// to every other build in the project and we don't want it to fail.
object Build {
	val KOTLIN_JVM_TARGET = JvmTarget.JVM_1_8
	const val JAVAC_RELEASE_OPT = "--release=8"
}

tasks {
	withType<JavaCompile>().configureEach {
		options.compilerArgs.add(Build.JAVAC_RELEASE_OPT)
	}
	withType<KotlinCompile>().configureEach(fun(task) = with(task.compilerOptions) {
		task.jvmTargetValidationMode.set(JvmTargetValidationMode.IGNORE)
		jvmTarget.set(Build.KOTLIN_JVM_TARGET)

		apiVersion.set(KOTLIN_LANGUAGE_VERSION_USED_IN_COMPILE)
		languageVersion.set(KOTLIN_LANGUAGE_VERSION_USED_IN_COMPILE)
	})
}

// --

fun loadBaseProps(path: String) = Properties().apply {
	ByteArrayInputStream(
		providers.fileContents(layout.projectDirectory.file(path))
			.asBytes.orNull ?: return@apply
	).use {
		load(it)
	}
}

kotlin.sourceSets.main {
	kotlin.srcDir(tasks.register("buildConfig", fun(task) {
		val project = task.project

		val outputDir = project.layout.buildDirectory.dir("generated/${task.name}")
		task.outputs.dir(outputDir)

		task.doFirst(fun(task) {
			val out = StringBuilder()
			out.appendLine("package build.base")
			out.appendLine()

			BaseProps.forEach { (k, v) ->
				out.append("const val ")
				out.append(k)
				out.append(" = \"\"\"")
				out.append(v.toString().replace("$", "\${'$'}"))
				out.appendLine("\"\"\"")
			}

			File(outputDir.get().asFile, "build/base/${task.name}.kt").outputStream().use {
				it.write(out.toString().encodeToByteArray())
			}
		})
	}))
}
