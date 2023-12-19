import org.gradle.kotlin.dsl.support.expectedKotlinDslPluginsVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion as KotlinCompileVersion

plugins {
	`java-gradle-plugin`
	`kotlin-dsl-base`
}

group = "build"

val pluginsDir = "src/main/plugins"

gradlePlugin {
	plugins {
		addPlugin("build.kt.base")

		addPlugin("build.plugins.base")
		addPlugin("build.root")

		addPlugin("build.dependencies")
		addPlugin("build.dotbuild")
		addPlugin("build.settings.buildslist")
	}
}

//#region Complex build setup

internal object Build {
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
	val KOTLIN_JVM_TARGET = JvmTarget.JVM_1_8
	const val JAVAC_RELEASE_OPT = "--release=8"
}

kotlin.sourceSets {
	main {
		project.objects.sourceDirectorySet("plugins", "plugins").run {
			srcDir(pluginsDir)
			include("**/${Build.PLUGIN_CLASS}.kt")
			kotlin.source(this)
		}
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
		freeCompilerArgs.add("-Xjvm-default=all")
	})
}

//#region Utilities

fun NamedDomainObjectContainer<PluginDeclaration>.addPlugin(name: String) {
	create(name) {
		buildString {
			append(pluginsDir)
			for (it in name.split('.')) append('/').append(it)
			append('/').append("${Build.PLUGIN_CLASS}.kt")
		}.let {
			require(file(it).isFile) { "Plugin file not found: $it" }
		}
		id = name
		implementationClass = "$name.${Build.PLUGIN_CLASS}"
	}
}

fun DependencyHandler.compileOnlyApiTestImpl(dependencyNotation: Any) {
	compileOnlyApi(dependencyNotation)
	testImplementation(dependencyNotation)
}

//#endregion
//#endregion

tasks.test {
	useJUnitPlatform()
}

dependencies {
	compileOnlyApiTestImpl(embeddedKotlin("gradle-plugin"))
	api("org.gradle.kotlin:gradle-kotlin-dsl-plugins:$expectedKotlinDslPluginsVersion")

	testImplementation(embeddedKotlin("test"))
}
