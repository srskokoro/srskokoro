import org.gradle.kotlin.dsl.support.expectedKotlinDslPluginsVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.security.MessageDigest
import java.util.HexFormat
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion as KotlinCompileVersion

plugins {
	`java-gradle-plugin`
	`kotlin-dsl-base`
}

group = "build"

gradlePlugin {
	plugins {
		addPlugin("build.kt.base")

		addPlugin("build.plugins")
		addPlugin("build.plugins.base")
		addPlugin("build.root")

		addPlugin("build.support.kt.base")
		addPlugin("build.support.kt.jvm")
		addPlugin("build.support.kt.mpp")

		addPlugin("build.dependencies")
		addPlugin("build.dotbuild")
		addPlugin("build.settings.buildslist")
	}
}

/**
 * NOTE: The setup of this build is similar to [build.plugins.base._plugin] and
 * should be kept consistent with that (as much as possible).
 */
object _eat_comment_

//#region Complex build setup

internal object Build {
	const val PLUGINS_DIR = "src/main/plugins"
	const val PLUGIN_CLASS = "_plugin"

	// NOTE: The following ensures that our convention plugins are always
	// compiled with a consistent JVM bytecode target version. Otherwise, the
	// compiled output would vary depending on the current JDK running Gradle.
	// Now, we don't want to alter the JVM toolchain, since not only that it
	// would download an entirely separate JDK, but it would also affect our
	// dependencies, e.g., it would force a specific variant of our dependencies
	// to be selected in order to conform to the current JDK (and it would
	// otherwise throw if it can't do so). We want none of that hassle when we
	// just want our target bytecode to be consistent. Additionally, not only
	// that we want consistency, we also want the output's bytecode version to
	// be as low as it can reasonably be, i.e., Java `1.8`, as Android Studio
	// currently expects that, or it'll complain stuffs like "cannot inline
	// bytecode built with JVM target <higher version>…" etc., when the build
	// isn't even complaining that.
	inline val KOTLIN_JVM_TARGET get() = JvmTarget.JVM_1_8
	const val JAVAC_RELEASE_OPT = "--release=8"
}

/** @see build.api.testing.TestSystemProps */
internal object TestSystemProps {
	const val TMPDIR = "build.api.testing.io.tmpdir"
	const val CLASSPATH = "build.api.testing.classpath"
}

configureMain {
	project.objects.sourceDirectorySet("plugins", "plugins").run {
		srcDir(Build.PLUGINS_DIR)
		include("**/${Build.PLUGIN_CLASS}.kt")
		kotlin.source(this)
	}
}

tasks {
	withType<JavaCompile>().configureEach {
		options.compilerArgs.add(Build.JAVAC_RELEASE_OPT)
	}
	withType<KotlinCompile>().configureEach(fun(task) = with(task.compilerOptions) {
		task.jvmTargetValidationMode.set(JvmTargetValidationMode.IGNORE)
		jvmTarget.set(Build.KOTLIN_JVM_TARGET)

		val kotlinVersion = KotlinCompileVersion.DEFAULT
		apiVersion.set(kotlinVersion)
		languageVersion.set(kotlinVersion)

		freeCompilerArgs.apply {
			add("-Xjvm-default=all")
		}
	})
	withType<Test>().configureEach {
		val taskTmpDir = temporaryDir

		val ioTmpDir = File(taskTmpDir, "io")
		systemProperty("java.io.tmpdir", ioTmpDir)

		val buildTmpDir = File(taskTmpDir, "b")
		systemProperty(TestSystemProps.TMPDIR, buildTmpDir)

		doFirst {
			ioTmpDir.mkdir() // …or Gradle will warn us about its nonexistence
			buildTmpDir.deleteRecursively()
		}
	}
}
afterEvaluate {
	tasks.withType<Test>().configureEach {
		val s = classpath.joinToString(File.pathSeparator)
		systemProperty(TestSystemProps.CLASSPATH, s)
	}
}

//#region Utilities

fun NamedDomainObjectContainer<PluginDeclaration>.addPlugin(name: String) {
	create(name) {
		buildString {
			append(Build.PLUGINS_DIR)
			for (it in name.split('.')) append('/').append(it)
			append('/').append("${Build.PLUGIN_CLASS}.kt")
		}.let {
			require(file(it).isFile) { "Plugin file not found: $it" }
		}
		id = name
		implementationClass = "$name.${Build.PLUGIN_CLASS}"
	}
}

fun configureMain(action: Action<in KotlinSourceSet>) {
	kotlin.sourceSets {
		named("main", action)
	}
}

fun DependencyHandler.compileOnlyApiTestImpl(dependencyNotation: Any) {
	compileOnlyApi(dependencyNotation)
	testImplementation(dependencyNotation)
}

//#endregion
//#endregion

//#region Special build setup for `build.dependencies` plugin


//#endregion

tasks.test {
	useJUnitPlatform()
	jvmArgs("-ea") // Also enables stacktrace recovery for kotlinx coroutines
}

dependencies {
	compileOnlyApiTestImpl(embeddedKotlin("gradle-plugin"))
	api("org.gradle.kotlin:gradle-kotlin-dsl-plugins:$expectedKotlinDslPluginsVersion")

	testImplementation(embeddedKotlin("test"))

	// NOTE: Deliberately not `api` since it's used only for compiling our test
	// utilities which should really only be referenced during tests (by both
	// the current project and its consumers).
	compileOnly(gradleTestKit())
}
