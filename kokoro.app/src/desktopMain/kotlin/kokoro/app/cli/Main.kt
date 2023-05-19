package kokoro.app.cli

import TODO
import com.github.ajalt.clikt.core.subcommands
import kotlinx.coroutines.CoroutineScope

class Main : BaseCommand(invokeWithoutSubcommand = true) {
	init {
		subcommands(
			Open(),
		)
	}

	override suspend fun CoroutineScope.execute() {
		TODO { IMPLEMENT("Display 'collection selection' window") }
	}
}
