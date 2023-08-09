package kokoro.app.ui

import java.awt.Component
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

class TopLevelComponentRef(component: Component?) : WeakReference<Component?>(component), CoroutineContext.Element {

	companion object Key : CoroutineContext.Key<TopLevelComponentRef>

	override val key: CoroutineContext.Key<*> get() = TopLevelComponentRef
}
