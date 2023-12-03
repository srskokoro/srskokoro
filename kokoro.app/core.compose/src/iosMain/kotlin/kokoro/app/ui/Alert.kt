package kokoro.app.ui

import androidx.compose.runtime.*

//#region

@Composable
actual inline fun Alert(
	spec: AlertSpec,
	interceptor: AlertInterceptor,
	crossinline recipient: (AlertButton?) -> Unit,
): Unit = TODO("Not yet implemented")

//#endregion

//#region Style

actual enum class AlertStyle {
	PLAIN,
	ERROR,
	WARN,
	QUESTION,
	INFO,
}

//#endregion

//#region Buttons

actual sealed interface AlertButton {
	actual val choice: AlertChoice
	actual val textOverride: Any?
}

actual enum class AlertChoice : AlertButton {
	OK,
	Cancel,
	Yes,
	No,
	CustomAction,
	;

	actual override val choice: AlertChoice get() = this
	actual override val textOverride: Nothing? get() = null
}

//#endregion
