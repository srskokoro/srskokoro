package kokoro.app.ui.engine.window

import kotlin.jvm.JvmField
import kotlin.jvm.JvmInline

@JvmInline
value class WvWindowFactoryId private constructor(
	@JvmField val id: String,
) {
	override fun toString(): String = id

	val isNothing: Boolean inline get() = id === NOTHING.id

	companion object {

		/**
		 * @see WvWindowFactory.NOTHING
		 */
		val NOTHING: WvWindowFactoryId = wrap(::NOTHING.name)

		inline fun <reified W : WvWindow> of(tag: String?) = wrap(
			W::class.qualifiedName.toString().let { type ->
				if (tag == null) type else "$type#$tag"
			},
		)

		/**
		 * @see WvWindowFactoryId.of
		 */
		fun wrap(id: String) = WvWindowFactoryId(id)
	}
}
