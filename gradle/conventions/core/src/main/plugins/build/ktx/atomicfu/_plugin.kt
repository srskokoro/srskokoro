package build.ktx.atomicfu

import build.api.ProjectPlugin
import build.api.dsl.*
import kotlinx.atomicfu.plugin.gradle.AtomicFUPluginExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin("kotlinx-atomicfu")
	}

	check(
		providers.gradleProperty("kotlin.js.compiler").get().let { it == "ir" || it == "both" } &&
			providers.gradleProperty("kotlinx.atomicfu.enableJvmIrTransformation").get() == "true" &&
			providers.gradleProperty("kotlinx.atomicfu.enableNativeIrTransformation").get() == "true" &&
			providers.gradleProperty("kotlinx.atomicfu.enableJsIrTransformation").get() == "true"
	) {
		"""
		Proper setup of IR transformation mode is required.
		- See, https://github.com/Kotlin/kotlinx-atomicfu/blob/0.23.2/README.md#atomicfu-compiler-plugin
		""".trimIndent()
	}

	setUp(x<AtomicFUPluginExtension>("atomicfu"))
})

private fun Project.setUp(atomicfu: AtomicFUPluginExtension): Unit = with(atomicfu) {
	// Necessary or AtomicFU won't add the project dependencies automatically.
	dependenciesVersion = deps?.versions?.module("org.jetbrains.kotlinx:atomicfu-gradle-plugin")
	// ^ Needed since (at the time of writing) AtomicFU sets it up by inferring
	// the version from the root project's `buildscript.dependencies` -- which
	// is bound to be absent, given that we resolve dependency versions in a
	// different (and customized) way.
}
