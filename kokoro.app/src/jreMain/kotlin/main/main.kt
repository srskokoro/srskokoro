package main

import com.formdev.flatlaf.FlatDarkLaf
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
