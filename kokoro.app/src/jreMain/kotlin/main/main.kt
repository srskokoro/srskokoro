package main

import kokoro.app.ui.swing.AppLaf
import kokoro.app.ui.swing.AppLafNatives
import kokoro.app.ui.swing.BaseWindowFrame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import java.awt.Dimension
import javax.swing.JLabel

fun main(): Unit = runBlocking(Dispatchers.Swing) {
	AppLafNatives.init()
	println("Hello World!")

	AppLaf.DarkMode.USE_SYSTEM.setUp()
	BaseWindowFrame(appHomeExe.toString()).apply {
		contentPane.add(JLabel("Hello World!", JLabel.CENTER).apply {
			preferredSize = Dimension(250, 250)
		})
	}.apply {
		pack()
		minimumSize = Dimension(15, 250)
		isLocationByPlatform = true
		isVisible = true
	}
}
