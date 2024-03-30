package kokoro.app.ui.swing

import java.awt.EventQueue

/**
 * Runs the specified [block] on the Swing EDT. If the current thread is the
 * Swing EDT, the given [block] will run immediately; otherwise, the [block]
 * will be scheduled for later execution on the Swing EDT.
 *
 * @see withThreadSwing
 */
inline fun doOnThreadSwing(crossinline block: () -> Unit) {
	if (EventQueue.isDispatchThread()) block()
	else EventQueue.invokeLater { block() }
}

/**
 * @see doOnThreadSwing
 * @see withThreadSwing
 */
fun doOnThreadSwing(runnable: Runnable) {
	if (EventQueue.isDispatchThread()) runnable.run()
	else EventQueue.invokeLater(runnable)
}

/**
 * Runs the specified [block] on the Swing EDT. The call blocks until this has
 * happened.
 *
 * @see doOnThreadSwing
 */
inline fun withThreadSwing(crossinline block: () -> Unit) {
	if (EventQueue.isDispatchThread()) block()
	else EventQueue.invokeAndWait { block() }
}

/**
 * @see withThreadSwing
 * @see doOnThreadSwing
 */
fun withThreadSwing(runnable: Runnable) {
	if (EventQueue.isDispatchThread()) runnable.run()
	else EventQueue.invokeAndWait(runnable)
}
