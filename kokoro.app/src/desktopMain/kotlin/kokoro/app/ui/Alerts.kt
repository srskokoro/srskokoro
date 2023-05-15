package kokoro.app.ui

actual suspend fun Alerts.await(handler: AlertHandler, spec: AlertSpec): AlertButton? = TODO("Not yet implemented")

actual enum class AlertStyle {
	PLAIN,
	ERROR,
	WARN,
	QUESTION,
	INFO,
}

actual sealed interface AlertButton {
	actual val choice: AlertChoice
	actual val textOverride: Any?
}

actual enum class AlertChoice : AlertButton {
	OK,
	Cancel,
	Yes,
	No,
	;

	actual override val choice: AlertChoice get() = this
	actual override val textOverride: Nothing? get() = null
}
