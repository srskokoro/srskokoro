package kokoro.build.jcef

import build.support.Os
import build.support.OsArch

internal object JcefPlatform {

	@JvmField val ID = buildString {
		append(when (Os.current) {
			Os.WINDOWS -> "windows"
			Os.MACOS -> "macosx"
			Os.LINUX -> "linux"
		})
		append('-')
		append(when (OsArch.current) {
			OsArch.X86 -> "i386"
			OsArch.X86_64 -> "amd64"
			OsArch.AARCH64 -> "arm64"
		})
	}

	@JvmField val requiredJvmArgs = if (Os.current == Os.MACOS) listOf(
		"--add-opens", "java.desktop/sun.awt=ALL-UNNAMED",
		"--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED",
		"--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED",
	) else emptyList()
}
