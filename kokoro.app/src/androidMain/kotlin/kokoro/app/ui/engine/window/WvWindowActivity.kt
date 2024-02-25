package kokoro.app.ui.engine.window

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class WvWindowActivity : ComponentActivity() {

	companion object {

		private val COMPONENT_CLASS_NAME = WvWindowActivity::class.java.name

		fun shouldHandle(intent: Intent): Boolean {
			return intent.component?.className == COMPONENT_CLASS_NAME
		}
	}

	private var handle: WvWindowHandleImpl? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		WvWindowHandleImpl.get(intent)?.let { h ->
			handle = h
			h.attachContext(this@WvWindowActivity)
			return // Success. Skip code below.
		}

		// Either the `Intent` isn't supported or there isn't enough information
		// to process the request.
		finish()
	}

	override fun onDestroy() {
		if (isFinishing) handle?.run {
			detachContext() // So that `finishAndRemoveTask()` isn't called by `close()` below
			close()
		}
		super.onDestroy()
	}
}
