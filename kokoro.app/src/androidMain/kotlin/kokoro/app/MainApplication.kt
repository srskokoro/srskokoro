package kokoro.app

import android.app.Application

class MainApplication : Application() {
	init {
		`$$inst` = this
	}

	companion object {
		@Suppress("ObjectPropertyName")
		@JvmField
		internal var `$$inst`: Application? = null
	}
}
