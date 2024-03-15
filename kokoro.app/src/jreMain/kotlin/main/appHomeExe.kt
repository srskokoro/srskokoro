package main

import java.io.File
import kotlin.jvm.optionals.getOrNull

@JvmField val appHomeExe = run(fun(): String? {
	ProcessHandle.current().info().command().getOrNull()?.let { exePath ->
		val exeFile = File(exePath).absoluteFile
		if (exeFile.parentFile == appHomeDir) {
			return exeFile.name
		}
	}
	return System.getenv("APP_BASE_NAME")
})
