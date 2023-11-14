package kokoro.jcef

import org.cef.callback.CefSchemeRegistrar

fun interface JcefCustomSchemesRegistrant {

	/** @see org.cef.handler.CefAppHandler.onRegisterCustomSchemes */
	fun onRegisterCustomSchemes(registrar: CefSchemeRegistrar)
}
