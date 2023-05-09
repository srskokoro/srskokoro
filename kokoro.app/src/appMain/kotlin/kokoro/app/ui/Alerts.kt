package kokoro.app.ui

expect object Alerts {

	suspend inline fun await(crossinline spec: AlertSpec.() -> Unit): AlertChoice

	suspend fun await(spec: AlertSpec): AlertChoice
}
