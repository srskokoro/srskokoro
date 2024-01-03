import org.gradle.kotlin.dsl.support.expectedKotlinDslPluginsVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion as KotlinCompileVersion

plugins {
	`java-gradle-plugin`
	`kotlin-dsl-base`
}

gradlePlugin {
	plugins {
		addPlugin("build.conventions")
		addPlugin("build.conventions.root")
		addPlugin("build.conventions.support")
	}
}

//#region Complex build setup

private object Build {
	const val PLUGIN_CLASS = "_plugin"

	const val JAVA_RELEASE_TARGET = 8
	inline val KOTLIN_JVM_TARGET get() = JvmTarget.JVM_1_8
}

tasks {
	compileJava.configure {
		options.release = Build.JAVA_RELEASE_TARGET
	}
	compileKotlin.configure {
		compilerOptions {
			jvmTarget = Build.KOTLIN_JVM_TARGET
			apiVersion.set(KotlinCompileVersion.DEFAULT)
			languageVersion.set(KotlinCompileVersion.DEFAULT)
			freeCompilerArgs.add("-opt-in=build.conventions.internal.InternalConventionsApi")
		}
	}
}

fun NamedDomainObjectContainer<PluginDeclaration>.addPlugin(name: String) {
	create(name) {
		id = name
		implementationClass = "$name.${Build.PLUGIN_CLASS}"
	}
}

//#endregion

dependencies {
	compileOnly(embeddedKotlin("gradle-plugin"))
	compileOnly("org.gradle.kotlin", "gradle-kotlin-dsl-plugins", expectedKotlinDslPluginsVersion)
	compileOnly(embeddedKotlin("sam-with-receiver"))
	compileOnly(embeddedKotlin("assignment"))
}
