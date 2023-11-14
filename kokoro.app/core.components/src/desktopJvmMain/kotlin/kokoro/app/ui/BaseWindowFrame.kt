package kokoro.app.ui

import kokoro.internal.ui.assertThreadSwing
import java.awt.GraphicsConfiguration
import java.awt.event.WindowEvent
import java.lang.ref.WeakReference
import javax.swing.JFrame

open class BaseWindowFrame @JvmOverloads constructor(
	title: String = DEFAULT_TITLE,
	gc: GraphicsConfiguration? = DEFAULT_GRAPHICS_CONFIGURATION,
) : JFrame(title, gc) {

	init {
		setUp()
	}

	private fun setUp() {
		defaultCloseOperation = DISPOSE_ON_CLOSE
	}

	companion object {
		const val DEFAULT_TITLE = ""
		inline val DEFAULT_GRAPHICS_CONFIGURATION: GraphicsConfiguration? get() = null

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
