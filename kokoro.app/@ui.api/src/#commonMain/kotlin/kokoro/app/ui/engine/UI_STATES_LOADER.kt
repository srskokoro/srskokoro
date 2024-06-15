package kokoro.app.ui.engine

/**
 * A JS function (under [`globalThis`](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/globalThis))
 * provided to a web view, used for state keeping.
 *
 * It loads any states saved before and returns it as a string. Depending on the
 * implementation and current platform, it may also require an argument to be
 * passed to it: a handler function for aggregating and encoding new states to
 * save, with the return value also a string.
 *
 * Must only be called once. Throws otherwise.
 */
const val UI_STATES_LOADER = "uiSs"
