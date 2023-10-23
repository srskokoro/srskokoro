package main.cli

import TODO
import com.github.ajalt.clikt.core.subcommands
import main.cli.engine.BaseMain

internal open class ClientMain : BaseMain() {
	init {
		subcommands(
			Open(),
		)
	}

	override suspend fun execute() {
		if (currentContext.invokedSubcommand != null) return
		TODO { IMPLEMENT("Display 'collection selection' window") }
	}
}
