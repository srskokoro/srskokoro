package kokoro.app.ui.wv.modifier

import app.cash.redwood.Modifier
import kokoro.app.ui.wv.ArgumentsBuilder

abstract class GlobalModifier(
	modifierId: Int,
) : ModifierDelegate<Modifier>(modifierId), Modifier.Element {

	abstract fun ArgumentsBuilder.onBind()

	final override fun ArgumentsBuilder.onBind(modifier: Modifier) = onBind()
}
