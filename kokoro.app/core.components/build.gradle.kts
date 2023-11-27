plugins {
	id("kokoro.conv.kt.mpp.lib")
	id("conv.ktx.atomicfu")
}

dependencies {
	commonMainImplementation(project(":kokoro:internal"))

	commonMainImplementation(project(":kokoro.app:core.base"))

	appMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	desktopJvmMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing")

	desktopJvmMainApi("com.formdev:flatlaf")
	desktopJvmMainApi("com.formdev:flatlaf-extras")
	// See, https://www.formdev.com/flatlaf/native-libraries/
	// TODO! Auto-detect which native library to use for current OS
	desktopJvmMainImplementation("com.formdev:flatlaf::linux-x86_64@so")
	desktopJvmMainImplementation("com.formdev:flatlaf::windows-x86_64@dll")
	desktopJvmMainImplementation("com.formdev:flatlaf::windows-x86@dll")

	desktopJvmMainImplementation("com.github.Dansoftowner:jSystemThemeDetector")
	desktopJvmMainImplementation("org.slf4j:slf4j-jdk14") // Needed for `jSystemThemeDetector`
}
