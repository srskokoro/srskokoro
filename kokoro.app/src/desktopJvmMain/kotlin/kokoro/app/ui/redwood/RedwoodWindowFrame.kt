package kokoro.app.ui.redwood

import androidx.compose.runtime.*
import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.widget.RedwoodView
import app.cash.redwood.widget.Widget
import kokoro.app.ui.ScopedWindowFrame
import kokoro.app.ui.compose.SimpleFrameClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import java.awt.EventQueue
import java.awt.Graphics
import java.awt.GraphicsConfiguration
import kotlin.coroutines.CoroutineContext

open class RedwoodWindowFrame @JvmOverloads constructor(
	context: CoroutineContext = DEFAULT_CONTEXT,
	title: String = DEFAULT_TITLE,
	gc: GraphicsConfiguration? = DEFAULT_GRAPHICS_CONFIGURATION,
	scopeFactory: ScopeFactory = DEFAULT_SCOPE_FACTORY,
) : ScopedWindowFrame(
	context = context,
	title = title,
	gc = gc,
	scopeFactory,
) {
	private var _composition: RedwoodComposition? = null
	val composition: RedwoodComposition
		get() = _composition ?: error("Must first call `init()`")

	/** @see composition */
	fun <W : Any> init(
		view: RedwoodView<W>,
		provider: Widget.Provider<W>,
		onEndChanges: () -> Unit = {},
	) {
		_composition?.cancel()
		_composition = RedwoodComposition(scope, view, provider, onEndChanges)
	}

	/** @see composition */
	fun <W : Any> init(
		container: Widget.Children<W>,
		uiConfigurations: StateFlow<UiConfiguration>,
		provider: Widget.Provider<W>,
		onEndChanges: () -> Unit = {},
	) {
		_composition?.cancel()
		_composition = RedwoodComposition(
			scope,
			container, uiConfigurations,
			provider, onEndChanges,
		)
	}

	override fun onCreateScope(scopeFactory: ScopeFactory, context: CoroutineContext): CoroutineScope {
		return super.onCreateScope(scopeFactory, context + (super.ref + FrameClock { repaint() }))
	}

	//#region Frame Clock Handling

	private val _frameClock = scope.coroutineContext[MonotonicFrameClock] as FrameClock
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
