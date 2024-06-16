package kokoro.app.ui.engine.window.webkit

import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import kokoro.internal.DEBUG

internal class WebChromeClientImpl : WebChromeClient() {

	companion object {
		/** @see WebChromeClientImpl */
		private const val TAG = "WebChromeClientImpl"

		init {
			// Ensures our log tag isn't too long.
			if (DEBUG) Log.isLoggable(TAG, Log.ERROR)
		}
	}

	override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
		val logLvl = when (val msgLvl = consoleMessage.messageLevel()) {
			ConsoleMessage.MessageLevel.ERROR -> Log.ERROR
			ConsoleMessage.MessageLevel.WARNING -> Log.WARN
			ConsoleMessage.MessageLevel.LOG, null -> Log.INFO
			ConsoleMessage.MessageLevel.DEBUG,
			ConsoleMessage.MessageLevel.TIP -> Log.DEBUG
			else -> {
				if (DEBUG || Log.isLoggable(TAG, Log.WARN))
					Log.w(TAG, "Unknown console message level: $msgLvl")
				Log.WARN
			}
		}
		if (DEBUG || Log.isLoggable(TAG, logLvl)) {
			Log.println(logLvl, TAG, buildString {
				append('[')
				append(consoleMessage.sourceId().ifEmpty { "<anonymous>" })
				append(':')
				append(consoleMessage.lineNumber())
				append(']')
				val msg = consoleMessage.message()
				append(if ('\n' in msg) '\n' else ' ')
				append(msg)
			})
		}
		return false
	}
}
