import convention.*

plugins {
	id("convention.kotlin.jvm")
	id("org.jetbrains.compose")
}

dependencies {
	setUpComposeDeps {
		implementation(it)
	}
	setUpComposePreviewDeps {
		// Needed only for preview
		implementation(it)
	}
	implementation(compose.desktop.currentOs)
}

// TODO Remove eventually. See also, https://github.com/JetBrains/compose-jb/pull/2515
// Workaround as it seems that configuring the JVM toolchain doesn't
// automatically set `compose.desktop.application.javaHome` still.
afterEvaluate {
	val app = compose.desktop.application
	if (app.javaHome == System.getProperty("java.home")) {
		@Suppress("UsePropertyAccessSyntax")
		val launcher = javaToolchains.launcherFor(java.toolchain).getOrNull()
		if (launcher != null) {
			app.javaHome = launcher.metadata.installationPath.asFile.absolutePath
		} else {
			logger.warn("Warning: JVM toolchain was not configured.")
		}
	} else {
		// May give us an indication of whether this workaround is still needed.
		logger.quiet("Custom `javaHome` set for `compose.desktop.application`:")
		logger.quiet("  ${app.javaHome}")
	}
}
