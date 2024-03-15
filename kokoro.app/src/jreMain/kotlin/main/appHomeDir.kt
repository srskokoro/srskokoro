package main

import java.io.File

@JvmField val appHomeDir = run(fun(): File {
	class X

	File(
		X::class.java.protectionDomain!!.codeSource.location.toURI(),
	).parentFile?.run {
		// The "app home" is the directory containing the "app" directory.
		if (name == "app") parentFile?.let {
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
