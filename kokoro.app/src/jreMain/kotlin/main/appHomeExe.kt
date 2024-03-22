package main

import java.io.File
import kotlin.jvm.optionals.getOrNull

@JvmField val appHomeExe = run(fun(): String? {
	ProcessHandle.current().info().command().getOrNull()?.let { exePath ->
		val exeFile = File(exePath).absoluteFile
		// TODO! Verify that the following checks are correct for all platforms
		run check@{
			// See, “Generated Application Image | Packaging Overview | Packaging Tool User's Guide”
			// -- https://docs.oracle.com/en/java/javase/21/jpackage/packaging-overview.html#GUID-DAE6A497-6E6F-4895-90CA-3C71AF052271
			val exeDir = exeFile.parentFile
			val appHomeDir = appHomeDir
			if (appHomeDir == exeDir) return@check
			exeDir.parentFile.let {
				if (appHomeDir == it) return@check
				if (appHomeDir == File(it, "lib")) return@check
			}
			return@let // All checks failed!
		}
		return exeFile.name
	}
	return System.getenv("APP_BASE_NAME")
})
