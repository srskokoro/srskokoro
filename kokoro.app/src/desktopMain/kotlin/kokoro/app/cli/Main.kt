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
		if (currentContext.invokedSubcommand != null) return
		TODO { IMPLEMENT("Display 'collection selection' window") }
	}
}
