package main.cli

import kokoro.internal.TODO
import main.cli.engine.BaseCommand

internal class Open : BaseCommand(name = "open") {
	override suspend fun execute() {
		TODO { IMPLEMENT("Display 'active decks' window") }
	}
}
