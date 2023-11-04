package kokoro.app.ui

import androidx.compose.runtime.*
import kokoro.app.ui.wv.WvWindowFrame
import java.awt.EventQueue
import java.awt.GraphicsConfiguration
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class AppWindowFrame @JvmOverloads constructor(
	context: CoroutineContext = EmptyCoroutineContext,
	spec: WindowSpec, args: List<Any?> = emptyList(),
	gc: GraphicsConfiguration? = DEFAULT_GRAPHICS_CONFIGURATION,
) : WvWindowFrame(context, DEFAULT_TITLE, gc), WindowHost, AppLafListener {

	companion object {
		init {
			// NOTE: LAFs need to be set up before window creation. The static
			// initializer for the class is therefore the best place to do this.
			ensureAppLaf()
		}
	}

	override val isDarkMode: Boolean
		get() = isDarkAppLaf

	override fun onAppLafUpdated() {
		updateUiConfiguration()
	}

	// --

	private val _state: WindowStateImpl
	val state: WindowState get() = _state

	init {
		val state = WindowStateImpl()
		_state = state
		spec.onNewArgs(state, args)
	}

	private inner class WindowStateImpl : AbstractWindowState() {
		var _title by mutableStateOf(DEFAULT_TITLE)
		override var title: String
			get() = _title
			set(value) {
				_title = value
				if (EventQueue.isDispatchThread()) {
					super@AppWindowFrame.setTitle(value)
				} else EventQueue.invokeLater {
					super@AppWindowFrame.setTitle(value)
				}
			}
	}

	override fun setTitle(title: String?) {
		_state._title = title ?: ""
		super.setTitle(title)
	}
}
