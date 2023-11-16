package kokoro.app.ui.engine.web

interface WebContextResolver {

	fun getWebContext(origin: WebOrigin): WebContext?
}
