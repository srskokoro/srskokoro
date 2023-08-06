package kokoro.app.ui.wv.modifier

import app.cash.redwood.Modifier

abstract class GlobalModifier : Modifier.Element {

	abstract fun ModifierBinder.onBind()
}
