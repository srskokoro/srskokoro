package kokoro.app.ui

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey

abstract class ScreenSpec : Screen, UiSerializable {
	final override val key = uniqueScreenKey

	@Composable
	abstract override fun Content()
}
