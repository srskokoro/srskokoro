package kokoro.app.ui.wv.modifier

import app.cash.redwood.Modifier
import kokoro.app.ui.wv.ArgumentsBuilder
import kotlin.jvm.JvmField

abstract class ModifierDelegate<T : Modifier>(
	@JvmField val modifierId: Int,
) {
	abstract fun ArgumentsBuilder.onBind(modifier: T)

	@Suppress("NOTHING_TO_INLINE")
	internal inline fun bind(args: ArgumentsBuilder, modifier: Modifier) {
		@Suppress("UNCHECKED_CAST")
		args.onBind(modifier as T)
	}
}
