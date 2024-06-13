package kokoro.app

import android.app.ActivityManager
import android.app.ActivityManager.AppTask
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.collection.IntObjectMap
import androidx.collection.MutableIntObjectMap
import androidx.core.content.getSystemService
import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain
import kokoro.internal.mainHandler
import kokoro.internal.os.taskIdCompat
import java.util.Locale

open class CoreApplication : Application() {

	override fun attachBaseContext(base: Context?) {
		super.attachBaseContext(base)
		@Suppress("DEPRECATION_ERROR")
		init = this
	}

	companion object {
		@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
		@PublishedApi @JvmField internal var init: CoreApplication? = null

		@Suppress("NOTHING_TO_INLINE")
		inline fun getOrNull() = @Suppress("DEPRECATION_ERROR") init

		@Suppress("NOTHING_TO_INLINE")
		inline fun get() = @Suppress("DEPRECATION_ERROR") Singleton.instance

		// --

		val activityManager inline get() = @Suppress("DEPRECATION_ERROR") _activityManager.value

		private var appTasks_: MutableIntObjectMap<AppTask>? = null

		@get:MainThread
		val appTasks: IntObjectMap<AppTask>
			get() {
				assertThreadMain()
				return appTasks_ ?: MutableIntObjectMap<AppTask>().apply {
					for (task in activityManager.appTasks) {
						set(task.taskInfo.taskIdCompat, task)
					}
					appTasks_ = this
					mainHandler.postAtFrontOfQueue {
						appTasks_ = null
					}
				}
			}

		@Suppress("NOTHING_TO_INLINE")
		inline fun finishAndRemoveTask(taskId: Int) =
			appTasks[taskId]?.finishAndRemoveTask()
	}

	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	@PublishedApi internal object Singleton {
		@JvmField val instance = getOrNull() ?: throw IllegalStateException(
			"Core application was not initialized."
		)
	}

	@Deprecated(SPECIAL_USE_DEPRECATION, level = DeprecationLevel.ERROR)
	@PublishedApi internal object _activityManager {
		@JvmField val value = get().getSystemService<ActivityManager>()
			?: error("`ActivityManager` seems unsupported")
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)

		// Update the JVM default `Locale` manually as it may not automatically
		// be updated for us, at least on some devices -- e.g., in the emulator,
		// it seems to be automatically updated. See also,
		// - https://medium.com/@hectorricardomendez/how-to-get-the-current-locale-in-android-fc12d8be6242
		// - https://stackoverflow.com/a/21844639
		// - https://developer.android.com/guide/topics/resources/app-languages
		//
		val locale = newConfig.locales.get(0)
		if (Locale.getDefault() != locale) {
			Locale.setDefault(locale)
		}
	}
}
