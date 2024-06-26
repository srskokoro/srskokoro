package main

import java.io.File

@JvmField val appHomeDir = run(fun(): File {
	appLibDir?.run {
		val name = name
		// The "app home" is the directory containing either the "app" directory
		// (when using `jpackage`) or the "lib" directory (when otherwise).
		if (name == "app" || name == "lib") parentFile?.let {
			return it
		}
	}

	System.getenv("APP_HOME")?.let {
		if (it.isNotBlank()) with(File(it)) {
			if (isDirectory) return absoluteFile
		}
	}

	throw Error("Could not resolve app home directory.\n" +
		"- The application distribution was likely not set up properly.")
})
