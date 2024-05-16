package build

import build.api.dsl.*
import com.android.build.api.dsl.ApplicationBaseFlavor
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget

internal fun Project.setUp(android: AndroidExtension) {
	if (!extra.parseBoolean("CHECK_ANDROID_LINT", isReleasing)) {
		removeLintFromCheckTask()
	}

	val compileOptions = android.compileOptions

	deps?.run {
		android.buildToolsVersion = prop("build.android.buildToolsVersion")
		android.compileSdk = prop("build.android.compileSdk").toInt()

		android.defaultConfig.run {
			val targetSdkValue = prop("build.android.targetSdk").toInt()
			if (this is ApplicationBaseFlavor) {
				targetSdk = targetSdkValue
			} else {
				android.lint.targetSdk = targetSdkValue
			}
			minSdk = prop("build.android.minSdk").toInt()
		}

		JavaVersion.forClassVersion(44 + prop("build.android.openjdk").toInt()).let {
			compileOptions.sourceCompatibility = it
			compileOptions.targetCompatibility = it
		}
	}

	@Suppress("UnstableApiUsage")
	android.testOptions.unitTests.run {
		isIncludeAndroidResources = true
	}

	compileOptions.run {
		isCoreLibraryDesugaringEnabled = true
	}
	dependencies.run {
		add("coreLibraryDesugaring", "com.android.tools:desugar_jdk_libs_nio")
	}
}

private fun setUpForAndroid(compilerOptions: KotlinJvmCompilerOptions, jvmTarget: JvmTarget?) {
	compilerOptions.jvmTarget = jvmTarget
	compilerOptions.freeCompilerArgs.add("-no-jdk")
}

internal fun setUpForAndroid(kotlin: KotlinMultiplatformExtension) {
	kotlin.targets.withType<KotlinAndroidTarget> {
		val jvmTarget = project.getJvmTargetForAndroid()
		compilations.all {
			val compilerOptions = compilerOptions.options
			setUpForAndroid(compilerOptions, jvmTarget)
			compilerOptions.noJdk = true // Set this here too because Android Studio is sometimes stupid
		}
	}
}

internal fun Project.setUpForAndroid(kotlin: KotlinAndroidProjectExtension) {
	setUpForAndroid(kotlin.compilerOptions, getJvmTargetForAndroid())
}

private fun Project.getJvmTargetForAndroid(): JvmTarget? =
	deps?.run { JvmTarget.entries[prop("build.android.openjdk").toInt() - 8 + JvmTarget.JVM_1_8.ordinal] }

// --

private fun Project.removeLintFromCheckTask() {
	afterEvaluate {
		tasks.named("check") {
			dependsOn.removeAll(fun(it): Boolean {
				when (it) {
					is String -> it
					is TaskProvider<*> -> it.name
					is Task -> it.name
					else -> return false
				}.let {
					return it == "lint"
				}
			})
		}
	}
}
