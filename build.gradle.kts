plugins {
	id("conv.root")

	// Avoid the plugins to be loaded multiple times in each subproject's
	// classloader. See also, https://youtrack.jetbrains.com/issue/KT-46200
	kotlin("jvm") apply false
	kotlin("multiplatform") apply false
	kotlin("android") apply false
	id("com.android.application") apply false
	id("com.android.library") apply false
	id("org.jetbrains.compose") apply false
	id("io.kotest.multiplatform") apply false
}

allprojects {
	group = "srs.kokoro"
}
