package main

import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatSystemProperties
import kokoro.app.ui.swing.BaseWindowFrame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import java.awt.Dimension
import java.io.File
import javax.swing.JLabel

fun main(): Unit = runBlocking(Dispatchers.Swing) {
	System.setProperty(FlatSystemProperties.NATIVE_LIBRARY_PATH, File(appHomeDir, "flatlaf").path)

	println("Hello World!")

	FlatDarkLaf.setup()
	BaseWindowFrame(appHomeExe.toString()).apply {
		contentPane.add(JLabel("Hello World!", JLabel.CENTER))
	}.apply {
		pack()
		minimumSize = Dimension(250, 250)
		isLocationByPlatform = true
		isVisible = true
	}
}
