package kokoro.app.ui.redwood

import androidx.compose.runtime.*
import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.widget.RedwoodView
import app.cash.redwood.widget.Widget
import kokoro.app.ui.BaseWindowFrame
import kokoro.app.ui.compose.SimpleFrameClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.plus
import java.awt.EventQueue
import java.awt.Graphics
import java.awt.GraphicsConfiguration

open class RedwoodWindowFrame : BaseWindowFrame {
	constructor() : super()
	constructor(gc: GraphicsConfiguration?) : super(gc)
	constructor(title: String?) : super(title)
	constructor(title: String?, gc: GraphicsConfiguration?) : super(title, gc)

	private var _composition: RedwoodComposition? = null
	val composition: RedwoodComposition
		get() = _composition ?: error("Must first call `init()`")

	/** @see composition */
	fun <W : Any> init(
		mainScope: CoroutineScope,
		view: RedwoodView<W>,
		provider: Widget.Provider<W>,
		onEndChanges: () -> Unit = {},
	) {
		_composition?.cancel()
		_composition = RedwoodComposition(
			wrapMainScope(mainScope),
			view, provider, onEndChanges,
		)
	}

	/** @see composition */
	fun <W : Any> init(
		mainScope: CoroutineScope,
		container: Widget.Children<W>,
		uiConfigurations: StateFlow<UiConfiguration>,
		provider: Widget.Provider<W>,
		onEndChanges: () -> Unit = {},
	) {
		_composition?.cancel()
		_composition = RedwoodComposition(
			wrapMainScope(mainScope),
			container, uiConfigurations,
			provider, onEndChanges,
		)
	}

	private fun wrapMainScope(mainScope: CoroutineScope) =
		mainScope + (_frameClock + super.ref)

	//#region Frame Clock Handling

	private val _frameClock = FrameClock { repaint() }
	val frameClock: MonotonicFrameClock get() = _frameClock

	private val nanoTimeStart = System.nanoTime()

	override fun paint(g: Graphics?) {
		super.paint(g)
		_frameClock.sendFrame(System.nanoTime() - nanoTimeStart)
	}

	private class FrameClock(
		private val onFrameNanosSwing: Runnable,
	) : SimpleFrameClock() {
		override fun onNewAwaiters() {
			EventQueue.invokeLater(onFrameNanosSwing)
		}
	}

	//#endregion
}
