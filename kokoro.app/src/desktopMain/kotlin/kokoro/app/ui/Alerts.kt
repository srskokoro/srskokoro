package kokoro.app.ui

import kokoro.internal.ui.checkThreadSwing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext

actual object Alerts {

	actual suspend inline fun await(crossinline spec: AlertSpec.() -> Unit) = await(AlertSpec().apply(spec))

	actual suspend inline fun await(spec: AlertSpec) = withContext(Dispatchers.Swing) { swing(spec) }

	inline fun swing(spec: AlertSpec.() -> Unit) = swing(AlertSpec().apply(spec))

	fun swing(spec: AlertSpec): AlertChoice {
		checkThreadSwing()
		TODO("Not yet implemented")
	}
}
