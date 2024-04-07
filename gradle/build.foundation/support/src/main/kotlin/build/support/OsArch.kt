package build.support

enum class OsArch {
	X86,
	X86_64,
	AARCH64,
	;

	companion object {

		val current: OsArch get() = current_ ?: throw E_Unknown()

		private val current_: OsArch?
		private val SYS_PROP_VAL: String?

		init {
			// Inspiration: https://github.com/JFormDesigner/FlatLaf/blob/3.4/flatlaf-core/src/main/java/com/formdev/flatlaf/util/SystemInfo.java#L72
			val v = System.getProperty("os.arch")
			SYS_PROP_VAL = v
			current_ = if (v != null) {
				if (v.equals("x86", ignoreCase = true)) X86
				else if (v.equals("amd64", ignoreCase = true)) X86_64
				else if (v.equals("x86_64", ignoreCase = true)) X86_64
				else if (v.equals("aarch64", ignoreCase = true)) AARCH64
				else null
			} else null
		}

		fun E_Unknown() = UnsupportedOperationException("Unknown OS architecture: $SYS_PROP_VAL")
	}
}
