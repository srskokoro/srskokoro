package kokoro.app.ui.engine

import kotlin.jvm.JvmField

/**
 * A simple, mutable value holder.
 */
data class UiState<T>(
	@JvmField var value: T?,
	@JvmField val bus: UiBus<T>,
)
