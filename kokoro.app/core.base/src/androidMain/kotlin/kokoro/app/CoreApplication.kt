package kokoro.app

import android.app.Application
import android.content.Context
import kokoro.internal.SPECIAL_USE_DEPRECATION

open class CoreApplication : Application() {

	override fun attachBaseContext(base: Context?) {
		super.attachBaseContext(base)
		@Suppress("DEPRECATION")
		init = this
	}

	companion object {
		@Deprecated(SPECIAL_USE_DEPRECATION)
		@PublishedApi @JvmField internal var init: CoreApplication? = null

		@Suppress("NOTHING_TO_INLINE")
		inline fun getOrNull() = @Suppress("DEPRECATION") init

		@Suppress("NOTHING_TO_INLINE")
		inline fun get() = Singleton.instance
	}

	@PublishedApi
	internal object Singleton {
		@JvmField val instance = getOrNull() ?: throw IllegalStateException(
			"Core application was not initialized."
		)
	}
}
