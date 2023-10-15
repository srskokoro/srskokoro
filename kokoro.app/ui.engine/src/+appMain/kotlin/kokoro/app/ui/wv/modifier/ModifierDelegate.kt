package kokoro.app.ui.wv.modifier

import app.cash.redwood.Modifier
import kokoro.app.ui.wv.ArgumentsBuilder

abstract class ModifierDelegate<T : Modifier> {

	abstract val templKey: String

	abstract fun ArgumentsBuilder.onBind(modifier: T)

	@Suppress("NOTHING_TO_INLINE")
	internal inline fun bind(args: ArgumentsBuilder, modifier: Modifier) {
		@Suppress("UNCHECKED_CAST")
		args.onBind(modifier as T)
	}

	companion object {
		inline operator fun <T : Modifier> invoke(
			crossinline templKey: () -> String,
			crossinline onBind: ArgumentsBuilder.(modifier: T) -> Unit,
		) = object : ModifierDelegate<T>() {
			override val templKey: String get() = templKey.invoke()
			override fun ArgumentsBuilder.onBind(modifier: T) = onBind.invoke(this, modifier)
		}
	}
}
