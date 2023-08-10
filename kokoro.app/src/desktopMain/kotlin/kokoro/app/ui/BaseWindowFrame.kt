package kokoro.app.ui

import kokoro.internal.ui.assertThreadSwing
import java.awt.GraphicsConfiguration
import java.awt.event.WindowEvent
import java.lang.ref.WeakReference
import javax.swing.JFrame

open class BaseWindowFrame : JFrame {
	constructor() : super()
	constructor(gc: GraphicsConfiguration?) : super(gc)
	constructor(title: String?) : super(title)
	constructor(title: String?, gc: GraphicsConfiguration?) : super(title, gc)

	companion object {
		/** @see javax.swing.JOptionPane.getRootFrame */
		val lastActive: BaseWindowFrame?
			get() {
				assertThreadSwing()
				return lastActiveRef.get()
			}

		private var lastActiveRef = WeakReference<BaseWindowFrame>(null)
	}

	@Suppress("LeakingThis")
	@JvmField protected val ref = TopLevelComponentRef(this)

	override fun processWindowEvent(e: WindowEvent) {
		super.processWindowEvent(e)

		if (e.id == WindowEvent.WINDOW_ACTIVATED) {
			lastActiveRef = ref
		}
	}
}
