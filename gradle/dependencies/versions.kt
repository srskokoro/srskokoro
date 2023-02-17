import convention.deps.internal.*
import org.gradle.jvm.toolchain.JvmVendorSpec

internal fun deps_versions.init() {
	jvm {
		ver = 17
		vendor = @Suppress("UnstableApiUsage") JvmVendorSpec.ADOPTIUM
	}

	val kotlin = "1.8.0"
	"org.jetbrains.kotlin".let {
		pluginGroup(it, kotlin)
		moduleGroup(it, kotlin)
	}

	val android = "7.4.0" // Android Gradle Plugin (AGP)
	pluginGroup("com.android", android)
	module("com.android.tools.build:gradle", android)

	val compose_mpp = "1.3.0-rc05"
	plugin("org.jetbrains.compose", compose_mpp)

	val kotest = "5.5.4"
	plugin("io.kotest.multiplatform", kotest)
	module("io.kotest:kotest-framework-multiplatform-plugin-gradle", kotest)
	module("io.kotest:kotest-runner-junit5", kotest)
	module("io.kotest:kotest-framework-engine", kotest)
	module("io.kotest:kotest-assertions-core", kotest)
	module("io.kotest:kotest-property", kotest)

	val kotlinx_coroutines = "1.6.4"
	module("org.jetbrains.kotlinx:kotlinx-coroutines-test", kotlinx_coroutines)
}
