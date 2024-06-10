package kokoro.app.ui.engine.web

/**
 * A custom TLD for app-level resources.
 *
 * App-level resources should only be accessible to other app-level resources.
 *
 * @see HOST_U
 */
const val HOST_X = "x"

/**
 * A custom TLD for user-level resources.
 *
 * User-level resources should only be accessible to other user-level resources
 * or to app-level resources.
 *
 * @see HOST_X
 */
const val HOST_U = "u"

// --

/** @see HTTPX_WV_X */
const val HOST_WV_X = "wv.$HOST_X"

/** @see HTTPX_UI_X */
const val HOST_UI_X = "ui.$HOST_X"

/** @see HTTPX_RES_X */
const val HOST_RES_X = "res.$HOST_X"

/** @see HTTPX_RES_U */
const val HOST_RES_U = "res.$HOST_U"
