package main

import android.app.Application

class IdeCheck : Application() {
	/**
	 * If everything is working as intended, the following should have no
	 * highlighting errors when analyzed by the IDE's code analysis mechanism.
	 */
	fun highlightTest() {
		buildString {
			append("foo")
		}
		StringBuilder::class.java.classLoader
	}
}
