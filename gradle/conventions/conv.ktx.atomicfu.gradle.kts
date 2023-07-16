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
