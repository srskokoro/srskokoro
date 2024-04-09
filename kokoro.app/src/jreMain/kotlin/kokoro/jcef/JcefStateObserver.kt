package kokoro.jcef

import org.cef.CefApp.CefAppState

fun interface JcefStateObserver {

	/** @see org.cef.handler.CefAppHandler.stateHasChanged */
	fun onStateChanged(state: CefAppState)

	/** @see org.cef.handler.CefAppHandler.onContextInitialized */
	fun onContextInitialized() = Unit
}
