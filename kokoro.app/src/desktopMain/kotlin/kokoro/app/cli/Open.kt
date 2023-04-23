package kokoro.app.cli

import kokoro.internal.kotlin.TODO
import kotlinx.coroutines.CoroutineScope

class Open : BaseCommand(name = "open") {
	override suspend fun CoroutineScope.execute() {
		TODO { IMPLEMENT("Display 'active decks' window") }
	}
}
