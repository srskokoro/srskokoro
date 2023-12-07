package kokoro.app.ui.engine.web

fun interface WebContextResolver {

	fun getWebContext(origin: WebOrigin): WebContext?
}
