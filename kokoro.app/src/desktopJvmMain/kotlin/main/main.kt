package main

import kotlinx.coroutines.launch
import main.cli.PrimaryMain
import java.awt.EventQueue

fun main(args: Array<out String>) {
	val main = PrimaryMain()
	val currentDir = System.getProperty("user.dir")

	val execState = try {
		main.feed(currentDir, args)
	} catch (_: ExitMain) {
		// Done. Do nothing else.
		return
	}

	val daemon = main.daemon
	if (daemon != null) {
		AppDaemon.ClientScope.launch {
			daemon.handleAppInstance {
				execState.transition()
			}
		}
		daemon.doWorkLoop() // Will block the current thread
		return // Done. Skip code below.
	}

	EventQueue.invokeLater {
		execState.consumeMessages()
	}
}
