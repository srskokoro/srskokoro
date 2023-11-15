package kokoro.app.ui.engine

import kokoro.jcef.Jcef

internal actual fun WvWebContext_platformInit() {
	Jcef.addCustomSchemes {
		it.addCustomScheme(
			/* schemeName = */ "wv",
			/* isStandard = */ true,
			/* isLocal = */ false,
			/* isDisplayIsolated = */ false,
			/* isSecure = */ true,
			/* isCorsEnabled = */ true,
			/* isCspBypassing = */ false,
			/* isFetchEnabled = */ true,
		)
	}
}

actual fun WvWebContext.Companion.appendOrigin(out: StringBuilder, webContextDomain: String): StringBuilder {
	return out.append("wv://$webContextDomain/")
}
