package main

import kokoro.app.ui.StackTraceModal
import kokoro.internal.throwAnySuppressed
import kotlinx.coroutines.launch
import main.cli.PrimaryMain
import java.awt.EventQueue

fun main(args: Array<out String>) {
	Thread.setDefaultUncaughtExceptionHandler(StackTraceModal) // Installed early to help with debugging

	val main = PrimaryMain()
	val currentDir = System.getProperty("user.dir")

	val execState = try {
		main.feed(currentDir, args)
	} catch (ex: ExitMain) {
		ex.throwAnySuppressed()
		return // Done. Do nothing else.
	}

	val daemon = main.daemon
	if (daemon != null) {
		// The following won't throw here (but may, in a separate coroutine).
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
