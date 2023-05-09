package kokoro.app.ui

actual object Alerts {

	actual suspend inline fun await(crossinline spec: AlertSpec.() -> Unit) = await(AlertSpec().apply(spec))

	actual suspend fun await(spec: AlertSpec): AlertChoice = TODO("Not yet implemented")
}
