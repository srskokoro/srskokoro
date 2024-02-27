package kokoro.app.ui.engine.window

import androidx.collection.MutableScatterMap
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain
import kokoro.internal.collections.initWithAssert

fun interface WvWindowFactory<out T : WvWindow> {

	fun init(context: WvContext): T

	companion object {
		/** WARNING: Must only be accessed (and modified) from the main thread. */
		private val map = MutableScatterMap<String, WvWindowFactory<*>>()

		/**
		 * @see register
		 * @see get
		 */
		inline fun <reified T : WvWindow> id(tag: String? = null): String {
			return T::class.qualifiedName.toString().let { type ->
				if (tag == null) type else "$type#$tag"
			}
		}

		/**
		 * @see id
		 * @see get
		 */
		@MainThread
		fun register(factory: WvWindowFactory<*>, id: String) {
			assertThreadMain()
			map.initWithAssert(id, factory, or = { "Factory ID already in use: $id" })
		}

		/**
		 * @see register
		 */
		@MainThread
		inline fun <reified T : WvWindow> register(factory: WvWindowFactory<T>) =
			register(factory, id<T>())

		/**
		 * @see register
		 */
		@MainThread
		inline fun <reified T : WvWindow> register(tag: String?, factory: WvWindowFactory<T>) =
			register(factory, id<T>(tag))

		/**
		 * @see register
		 */
		@MainThread
		fun get(id: String): WvWindowFactory<*>? {
			assertThreadMain()
			return map[id]
		}
	}
}
