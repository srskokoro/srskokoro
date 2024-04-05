package main

import java.io.File

@JvmField val appLibJar = run(fun(): File? {
	class X
	return X::class.java.protectionDomain
		?.codeSource
		?.location
		?.toURI()
		?.let { File(it) }
})
