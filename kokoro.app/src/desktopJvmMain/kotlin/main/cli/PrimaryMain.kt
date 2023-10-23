package main.cli

import main.AppDaemon
import main.setUpSingleProcessModel

internal class PrimaryMain : ClientMain() {
	internal var daemon: AppDaemon? = null

	override fun run() {
		setUpSingleProcessModel()
		super.run()
	}
}
