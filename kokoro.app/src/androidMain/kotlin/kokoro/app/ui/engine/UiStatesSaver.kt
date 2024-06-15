package kokoro.app.ui.engine

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.annotation.GuardedBy
import androidx.annotation.RequiresFeature
import androidx.annotation.UiThread
import androidx.collection.MutableScatterMap
import androidx.webkit.JavaScriptReplyProxy
import androidx.webkit.WebMessageCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import kokoro.app.ui.engine.web.PLATFORM_JS_SECRET
import kokoro.app.ui.engine.web.PLATFORM_JS_SECRET_n
import kokoro.internal.DEBUG
import kokoro.internal.collections.putIfAbsent
import kokoro.internal.notifyAll
import kokoro.internal.wait

@RequiresFeature(
	name = WebViewFeature.WEB_MESSAGE_LISTENER,
	enforcement = "androidx.webkit.WebViewFeature#isFeatureSupported",
)
internal class UiStatesSaver(
	private val oldUiStates: MutableScatterMap<String, String?>,
) : WebViewCompat.WebMessageListener {

	private class Entry(
		@JvmField val proxy: JavaScriptReplyProxy,
	) {
		@JvmField var saveable: String? = null
	}

	private val entries = MutableScatterMap<String, Entry>()
	@GuardedBy("entries") private var saveables = 0
	@GuardedBy("entries") private var encoding = false

	@UiThread
	override fun onPostMessage(
		view: WebView, message: WebMessageCompat,
		sourceOrigin: Uri, isMainFrame: Boolean,
		replyProxy: JavaScriptReplyProxy,
	) {
		val data = try {
			message.data!!
		} catch (ex: Throwable) {
			ex.printStackTrace()
			return // Skip code below
		}
		if (data.startsWith(WML_DEL_CHAR)) {
			// Case: entry deletion
			val key = data.substring(1)
			val entries = entries
			synchronized(entries) {
				val entry = entries[key] ?: return@synchronized
				if (entry.proxy == replyProxy) {
					entries.remove(key)
					if (entry.saveable != null) saveables--
					else entries.notifyAll()
				}
			}
		} else if (
			data.startsWith(PLATFORM_JS_SECRET) &&
			data.length > PLATFORM_JS_SECRET_n &&
			data[PLATFORM_JS_SECRET_n] == ' '
		) {
			// Case: entry creation
			val key = data.substring(PLATFORM_JS_SECRET_n + 1)
			val entry = Entry(replyProxy)
			val entries = entries
			synchronized(entries) {
				if (entries.putIfAbsent(key, entry) != null) {
					IllegalStateException("Forbidden. Entry already exists for key: $key")
						.printStackTrace()
				}
			}
		} else {
			IllegalStateException(E_FORBIDDEN).printStackTrace()
		}
	}

	@JavascriptInterface
	@JvmName(JSI_takeOld__name)
	fun takeOld(secret: String, key: String): String {
		check(secret == PLATFORM_JS_SECRET) { E_FORBIDDEN }
		val oldUiStates = oldUiStates
		return synchronized(oldUiStates) {
			oldUiStates.remove(key) ?: "{}"
		}
	}

	@JavascriptInterface
	@JvmName(JSI_saveNew__name)
	fun saveNew(secret: String, key: String, saveable: String) {
		check(secret == PLATFORM_JS_SECRET) { E_FORBIDDEN }
		val entries = entries
		synchronized(entries) {
			if (!encoding) {
				if (DEBUG) error("Called while not encoding")
				return@synchronized
			}
			val entry = entries[key]
			if (entry == null) {
				if (DEBUG) error("Entry not found for key: $key")
				return@synchronized
			}
			if (entry.saveable == null) {
				saveables++
				entries.notifyAll()
			}
			entry.saveable = saveable
		}
	}

	@SuppressLint("RequiresFeature")
	@UiThread
	fun encode(): UiStatesParcelable {
		val entries = entries
		synchronized(entries) {
			encoding = true
			try {
				try {
					saveables = 0
					entries.forEachValue {
						it.saveable = null
						it.proxy.postMessage("")
					}
				} catch (ex: Throwable) {
					try {
						saveables = entries.count { _, entry ->
							entry.saveable != null
						}
					} catch (exx: Throwable) {
						ex.addSuppressed(exx)
					}
					throw ex
				}
				while (saveables < entries.size) {
					entries.wait()
				}
				val out = UiStatesParcelable()
				val map = out.map
				entries.forEach { key, entry ->
					val saveable = entry.saveable ?: return@forEach
					if (saveable.isNotEmpty()) map[key] = saveable
				}
				return out
			} finally {
				encoding = false
			}
		}
	}

	companion object {
		private const val E_FORBIDDEN = "Forbidden"

		const val WML__name = "${UI_STATES_LOADER}_"
		private const val WML_DEL_CHAR: Char = '!'

		const val JSI__name = "${UI_STATES_LOADER}$"
		private const val JSI_takeOld__name = "t"
		private const val JSI_saveNew__name = "s"
	}

	object JS_DEF {
		private const val loader_global = UI_STATES_LOADER

		private const val wml_global = WML__name
		private const val wml = "_"
		private const val wml_del_char = WML_DEL_CHAR

		private const val jsi_global = JSI__name
		private const val jsi = "$"
		private const val jsi_saveNew = "$jsi.$JSI_saveNew__name"
		private const val jsi_takeOld = "$jsi.$JSI_takeOld__name"

		private const val secret = "W"
		private const val enc = "Z"

		private const val key = "k"
		private const val event = "e"
		private const val ex = "x"

		private const val unregistered = "u"
		private const val del_reg = "D"
		private const val add_reg = "A"

		private const val _true = "!0"
		private const val _false = "!1"

		private const val loader_def_body = "" +
			"\nlet " +
			/**/"$wml=$wml_global," +
			/**/"$jsi=$jsi_global" +
			"\n$loader_global=$wml_global=$jsi_global=null" +

			// Implementation references:
			// - “Spaces in URLs? | Stack Overflow” :: https://stackoverflow.com/a/5442701
			// - “Window: `name` property | Web APIs | MDN” :: https://developer.mozilla.org/en-US/docs/Web/API/Window/name
			"\nlet $key=location.href.split('#',1)+' '+name" +
			"\n$wml.onmessage=$event=>{" +
			/**/"try{$event=$enc()}" +
			/**/"catch($ex){$event='';setTimeout($event=>{throw $ex})}" +
			/**/"$jsi_saveNew($secret,$key,$event)" +
			"}" +

			// References regarding 'pagehide' and 'pageshow':
			// - https://www.igvita.com/2015/11/20/dont-lose-user-and-app-state-use-page-visibility/
			// - https://developer.chrome.com/docs/web-platform/page-lifecycle-api/image/page-lifecycle-api-state.svg
			// - https://developer.chrome.com/docs/web-platform/page-lifecycle-api

			"\nlet $unregistered=$_true" +
			"\nlet $del_reg=$event=>{" +
			/**/"if(!$unregistered){" +
			/**//**/"$unregistered=$_true;$wml.postMessage('$wml_del_char'+$key)" +
			/**/"}" +
			"}" +
			"\naddEventListener('pagehide',$del_reg)" +

			"\nlet $add_reg=$event=>{" +
			/**/"if($unregistered){" +
			/**//**/"$unregistered=$_false;$wml.postMessage($secret+' '+$key)" +
			/**/"}" +
			"}" +
			"\naddEventListener('pageshow',$add_reg)" +
			"\n$add_reg()" +

			"\nreturn $jsi_takeOld($secret,$key)"

		// --

		const val START = "\n$loader_global=(" +
			/**/"$secret=>$enc=>{" +
			/**//**/loader_def_body +
			/**/"\n}" +
			")('"
		const val END = "')"
	}
}
