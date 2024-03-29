package kokoro.app.ui.swing

import java.awt.Component
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

class TopLevelComponentRef<C : Component?>(component: C) : WeakReference<C>(component), CoroutineContext.Element {

	companion object Key : CoroutineContext.Key<TopLevelComponentRef<*>>

	override val key: CoroutineContext.Key<*> get() = TopLevelComponentRef

	override fun clear() {}
	override fun enqueue() = false
}
