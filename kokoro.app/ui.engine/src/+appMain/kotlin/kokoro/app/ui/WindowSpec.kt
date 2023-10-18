package kokoro.app.ui

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import kokoro.app.ui.wv.WvSetup

abstract class WindowSpec : Screen, UiSerializable {
	// TODO Access to `uniqueScreenKey` is the only reason we implemented
	//  `Screen` (which we would rather not implement). In the future, let's
	//  roll our own implementation of `uniqueScreenKey` and remove `Screen`.
	final override val key = uniqueScreenKey

	abstract fun newWvSetup(): WvSetup

	open fun onNewArgs(state: WindowState, args: List<Any?>) {
		state.updateArgs(args)
	}

	@Composable
	abstract override fun Content()
}
