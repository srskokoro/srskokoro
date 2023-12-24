import org.gradle.kotlin.dsl.support.expectedKotlinDslPluginsVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion as KotlinCompileVersion

plugins {
	`java-gradle-plugin`
	`kotlin-dsl-base`
}

group = "build"

gradlePlugin {
	plugins {
		//addPlugin("build.plugins.base")
		//addPlugin("build.root.base")
	}
}

//#region Complex build setup

private object Build {
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

configureMain {
	kotlin.source(project.objects.sourceDirectorySet("plugins", "plugins").apply {
		srcDir(Build.PLUGINS_DIR)
		include("**/${Build.PLUGIN_CLASS}.kt")
	})
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
	})
	withType<AbstractTestTask>().configureEach {
		doFirst {
			error(
				"Tests have been disallowed for this $project\n" + """

				NOTE: Complex logic should not reside in this build. This project should only
				provide simple utilities, helpers and base plugins, that all of which should not
				need testing.
				""".trimIndent()
			)
		}
	}
}

//#region Utilities

fun NamedDomainObjectContainer<PluginDeclaration>.addPlugin(name: String) {
	create(name) {
		buildString {
			append(Build.PLUGINS_DIR)
			for (s in name.split('.')) append('/').append(s)
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

//#endregion
//#endregion

dependencies {
	compileOnlyApi(embeddedKotlin("gradle-plugin"))
	api("org.gradle.kotlin:gradle-kotlin-dsl-plugins:$expectedKotlinDslPluginsVersion")
}
