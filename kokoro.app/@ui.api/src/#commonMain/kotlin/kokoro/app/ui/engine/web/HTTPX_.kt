package kokoro.app.ui.engine.web

import kotlin.jvm.JvmField

/** @see HOST_WV_X */
@JvmField val HTTPX_WV_X = "$HTTPX://$HOST_WV_X"

/** @see HOST_UI_X */
@JvmField val HTTPX_UI_X = "$HTTPX://$HOST_UI_X"

/** @see HOST_RES_X */
@JvmField val HTTPX_RES_X = "$HTTPX://$HOST_RES_X"

/** @see HOST_RES_U */
@JvmField val HTTPX_RES_U = "$HTTPX://$HOST_RES_U"

// --

@JvmField val HTTPX_WV_X_PLATFORM_JS = "$HTTPX_WV_X/platform.js"

@JvmField val HTTPX_UI_X_FAVICON = "$HTTPX_UI_X/favicon.ico"

@JvmField val HTTPX_RES_X_UI_JS = "$HTTPX_RES_X/ui.js"
