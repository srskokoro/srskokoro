import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	`lifecycle-base`
}

// The following ensures that our convention plugins are always compiled with a
// consistent JVM bytecode target version. Otherwise, the compiled output would
// vary depending on the current JDK running Gradle. Now, we don't want to alter
// the JVM toolchain, since not only that it would download an entirely separate
// JDK, but it would also affect our dependencies, e.g., it would force a
// specific variant of our dependencies to be selected in order to conform to
// the current JDK (and it would otherwise throw if it can't do so). We want
// none of that hassle when we just want our target bytecode to be consistent.
// Additionally, not only that we want consistency, we also want the output's
// bytecode version to be as low as it can reasonably be, i.e., Java `1.8`, as
// Android Studio currently expects that, or it'll complain stuffs like "cannot
// inline bytecode built with JVM target <higher version>…" etc., when the build
// isn't even complaining that.
//
internal object Build {
	val kotlinJvmTarget = JvmTarget.JVM_1_8
	const val javacReleaseOpt = "--release=8"
}
subprojects {
	pluginManager.withPlugin("org.gradle.kotlin.kotlin-dsl") {
		project.extra["kotlin.jvm.target.validation.mode"] = "ignore"
		tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
			compilerOptions.jvmTarget.set(Build.kotlinJvmTarget)
		}
		tasks.withType<JavaCompile>().configureEach {
			options.compilerArgs.add(Build.javacReleaseOpt)
		}
	}
}
