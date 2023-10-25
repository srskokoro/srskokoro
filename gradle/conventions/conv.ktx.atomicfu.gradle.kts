import conv.internal.setup.*

plugins {
	id("kotlinx-atomicfu")
}

check(
	providers.gradleProperty("kotlin.js.compiler").get().let { it == "ir" || it == "both" } &&
		providers.gradleProperty("kotlinx.atomicfu.enableJvmIrTransformation").get() == "true" &&
		providers.gradleProperty("kotlinx.atomicfu.enableJsIrTransformation").get() == "true"
) {
	"""
	Proper setup of IR transformation mode is required.
	- See, https://github.com/Kotlin/kotlinx-atomicfu/blob/0.21.0/README.md#atomicfu-compiler-plugin
	""".trimIndent()
}

atomicfu {
	// Necessary or AtomicFU won't add the project dependencies automatically.
	dependenciesVersion = deps?.run {
		val moduleId = "org.jetbrains.kotlinx:atomicfu-gradle-plugin"
		val v = modules.resolve(moduleId)
			?: error("Must set version for \"$moduleId\" in our dependency versions settings.")
		v.value
	}
	// ^ Needed since (at the time of writing) AtomicFU sets it up by inferring
	// the version from the root project's `buildscript.dependencies` -- which
	// is bound to be absent, given that we resolve dependency versions in a
	// different (and customized) way.
}
