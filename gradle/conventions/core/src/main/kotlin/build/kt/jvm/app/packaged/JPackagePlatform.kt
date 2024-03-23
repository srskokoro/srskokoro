package build.kt.jvm.app.packaged

enum class JPackagePlatform {
	WINDOWS,
	MACOS,
	LINUX,
	;

	companion object {

		val current = kotlin.run {
			// Inspiration: https://github.com/JFormDesigner/FlatLaf/blob/3.4/flatlaf-core/src/main/java/com/formdev/flatlaf/util/SystemInfo.java#L72
			val osName = System.getProperty("os.name")
			if (osName.startsWith("windows", ignoreCase = true)) WINDOWS
			else if (osName.startsWith("mac", ignoreCase = true)) MACOS
			else if (osName.startsWith("linux", ignoreCase = true)) LINUX
			else throw E_UnknownOs()
		}

		fun E_UnknownOs() = IllegalStateException("Unknown OS: ${System.getProperty("os.name")}")
	}
}
