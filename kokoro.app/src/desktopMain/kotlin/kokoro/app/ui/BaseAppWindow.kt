package kokoro.app.ui

import kokoro.internal.ui.assertThreadSwing
import java.awt.GraphicsConfiguration
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.lang.ref.WeakReference
import javax.swing.JFrame

open class BaseAppWindow : JFrame, WindowListener {
	constructor() : super()
	constructor(gc: GraphicsConfiguration?) : super(gc)
	constructor(title: String?) : super(title)
	constructor(title: String?, gc: GraphicsConfiguration?) : super(title, gc)

	init {
		@Suppress("LeakingThis")
		super.addWindowListener(this)
	}

	companion object {
		/** @see javax.swing.JOptionPane.getRootFrame */
		val lastActive: BaseAppWindow?
			get() {
				assertThreadSwing()
				return lastActiveRef.get()
			}

		private var lastActiveRef = WeakReference<BaseAppWindow>(null)
	}

	@Suppress("LeakingThis")
	private val ref = WeakReference(this)

	//region `WindowListener` implementation

	override fun windowOpened(e: WindowEvent) {}

	override fun windowClosing(e: WindowEvent) {}

	override fun windowClosed(e: WindowEvent) {}

	override fun windowIconified(e: WindowEvent) {}

	override fun windowDeiconified(e: WindowEvent) {}

	override fun windowActivated(e: WindowEvent) {
		lastActiveRef = ref
	}

	override fun windowDeactivated(e: WindowEvent) {}

	//endregion
}
