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
		@MainThread
		inline fun <reified T : WvWindow> id() = T::class.qualifiedName!!

		/**
		 * @see id
		 * @see get
		 */
		@MainThread
		inline fun <reified T : WvWindow> register(factory: WvWindowFactory<T>) =
			register(id<T>(), factory)

		/**
		 * @see register
		 */
		@MainThread
		fun register(id: String, factory: WvWindowFactory<*>) {
			assertThreadMain()
			map.initWithAssert(id, factory, or = { "Factory ID already in use: $id" })
		}

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
