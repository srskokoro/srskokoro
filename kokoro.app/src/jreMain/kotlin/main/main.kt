package main

import com.formdev.flatlaf.FlatSystemProperties
import java.io.File

fun main() {
	System.setProperty(FlatSystemProperties.NATIVE_LIBRARY_PATH, File(appHomeDir, "flatlaf").path)
	println("Hello World!")
}
