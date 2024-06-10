package com.formdev.flatlaf.ui

// TODO! Make a feature request to FlatLaf so that we don't have to do the following hack.
@JvmField val FlatNativeLibrary_isInitialized = FlatNativeLibrary::class.java.run {
	val f = getDeclaredField("initialized")
	f.isAccessible = true
	synchronized(this) { f.getBoolean(null) }
}
