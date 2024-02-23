package kokoro.app.ui.engine.window

import android.os.Bundle
import androidx.activity.ComponentActivity
import kokoro.internal.DEPRECATION_ERROR
import kokoro.internal.assertUnreachable

class WvWindowActivity : ComponentActivity() {

	companion object {
		init {
			WvWindowHandle_globalInit()
		}
	}

	private var handle: WvWindowHandle? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		intent.data?.let { uri ->
			val s = uri.toString()
			if (!s.startsWith("x:")) return@let

			val id = try {
				s.substring(2).toInt()
			} catch (ex: NumberFormatException) {
				assertUnreachable(ex)
				return@let // Fail
			}

			val h = WvWindowHandle.globalMap[id] ?: return@let
			handle = h

			@Suppress(DEPRECATION_ERROR)
			h.attach(this@WvWindowActivity)

			return // Success. Skip code below.
		}

		// Either the `Intent` isn't supported or there isn't enough information
		// to process the request.
		finish()
	}

	override fun onDestroy() {
		if (isFinishing) handle?.run {
			@Suppress(DEPRECATION_ERROR)
			detach() // So that `finishAndRemoveTask()` isn't called
			close()
		}
		super.onDestroy()
	}
}
