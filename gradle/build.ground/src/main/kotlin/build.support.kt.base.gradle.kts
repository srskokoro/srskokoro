import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
