package main

import kokoro.app.ui.swing.AppLaf
import kokoro.app.ui.swing.AppLafNatives
import kokoro.app.ui.swing.BaseWindowFrame
import kokoro.jcef.JcefNatives
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import java.awt.Dimension
import java.io.File
import javax.swing.JLabel

fun main(): Unit = runBlocking(Dispatchers.Swing) {
	JcefNatives.init(File(appHomeDir, "jcef"))
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
