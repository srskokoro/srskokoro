internal fun deps_versions.init() {
	val kotlin = "1.8.0"
	val android = "7.4.0" // Android Gradle Plugin (AGP)
	val compose_mpp = "1.3.0-rc05"
	val kotest = "5.5.4"
	val kotlinx_coroutines = "1.6.4"

	"org.jetbrains.kotlin".let {
		pluginGroup(it, kotlin)
		moduleGroup(it, kotlin)
	}

	pluginGroup("com.android", android)
	module("com.android.tools.build:gradle", android)

	plugin("org.jetbrains.compose", compose_mpp)
	plugin("io.kotest.multiplatform", kotest)

	module("org.jetbrains.kotlinx:kotlinx-coroutines-test", kotlinx_coroutines)
	module("io.kotest:kotest-runner-junit5", kotest)
	module("io.kotest:kotest-framework-engine", kotest)
	module("io.kotest:kotest-assertions-core", kotest)
	module("io.kotest:kotest-property", kotest)
}
