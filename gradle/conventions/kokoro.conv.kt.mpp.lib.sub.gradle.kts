import conv.internal.skipPlaceholderGenerationForKotlinTargetsConfigLoader

plugins {
	id("conv.sub.android")
}

skipPlaceholderGenerationForKotlinTargetsConfigLoader = true
apply(plugin = "kokoro.conv.kt.mpp.lib")
