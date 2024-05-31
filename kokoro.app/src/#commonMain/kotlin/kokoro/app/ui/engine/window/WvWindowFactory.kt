package kokoro.app.ui.engine.window

import androidx.collection.MutableScatterMap
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain
import kokoro.internal.check

/**
 * @see WvWindowFactory.from
 */
interface WvWindowFactory<out W : WvWindow> {

	@MainThread
	fun init(context: WvContext, isInitialState: Boolean): W

	companion object {

		inline fun <W : WvWindow> from(
			crossinline factory: (context: WvContext, isInitialState: Boolean) -> W,
		): WvWindowFactory<W> = object : WvWindowFactory<W> {
			override fun init(context: WvContext, isInitialState: Boolean): W = factory(context, isInitialState)
		}

		inline fun <W : WvWindow> from(
			crossinline factory: (context: WvContext) -> W,
		): WvWindowFactory<W> = object : WvWindowFactory<W> {
			override fun init(context: WvContext, isInitialState: Boolean): W = factory(context)
		}

		/**
		 * @see WvWindowFactoryId.NOTHING
		 */
		val NOTHING: WvWindowFactory<Nothing> = from { _, _ ->
			throw UnsupportedOperationException(
				"The ${::NOTHING.name} factory cannot be used to create windows."
			)
		}

		/** WARNING: Must only be accessed (and modified) from the main thread. */
		private val map = MutableScatterMap<String?, WvWindowFactory<*>>().also { map ->
			map[WvWindowFactoryId.NOTHING.id] = NOTHING
		}

		/**
		 * @see WvWindowFactoryId.of
		 * @see WvWindowFactoryId.NOTHING
		 */
		inline fun <reified W : WvWindow> id(tag: String? = null) =
			WvWindowFactoryId.of<W>(tag)

		/**
		 * @see WvWindowFactory.id
		 * @see WvWindowFactory.get
		 */
		@MainThread
		fun register(id: WvWindowFactoryId, factory: WvWindowFactory<*>) {
			assertThreadMain()
			map.compute(id.id) { _, v ->
				check(v == null, or = { "Factory ID already in use: $id" })
				factory
			}
		}

		/**
		 * @see register
		 */
		@MainThread
		inline fun <reified W : WvWindow> register(factory: WvWindowFactory<W>) = register(id<W>(), factory)

		/**
		 * @see register
		 */
		@MainThread
		inline fun <reified T : WvWindow> register(tag: String?, factory: WvWindowFactory<T>) = register(id<T>(tag), factory)

		/**
		 * @see register
		 */
		@Suppress("NOTHING_TO_INLINE")
		@MainThread
		inline fun get(id: WvWindowFactoryId) = get(id.id)

		/**
		 * @see register
		 * @see get
		 */
		@MainThread
		fun get(id: String?): WvWindowFactory<*>? {
			assertThreadMain()
			return map[id]
		}
	}
}
