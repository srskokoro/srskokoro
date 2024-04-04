package build.api.platform

enum class Os {
	WINDOWS,
	MACOS,
	LINUX,
	;

	companion object {

		val current: Os get() = current_ ?: throw E_Unknown()

		private val current_: Os?
		private val SYS_PROP_VAL: String?

		init {
			// Inspiration: https://github.com/JFormDesigner/FlatLaf/blob/3.4/flatlaf-core/src/main/java/com/formdev/flatlaf/util/SystemInfo.java#L72
			val v = System.getProperty("os.name")
			SYS_PROP_VAL = v
			current_ = if (v != null) {
				if (v.startsWith("windows", ignoreCase = true)) WINDOWS
				else if (v.startsWith("mac", ignoreCase = true)) MACOS
				else if (v.startsWith("linux", ignoreCase = true)) LINUX
				else null
			} else null
		}

		fun E_Unknown() = UnsupportedOperationException("Unknown OS: $SYS_PROP_VAL")
	}
}
