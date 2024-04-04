package main

import java.io.File

@JvmField val appLibDir = run(fun(): File? {
	class X
	return X::class.java.protectionDomain
		?.codeSource
		?.location
		?.toURI()
		?.let { File(it).parentFile }
})
