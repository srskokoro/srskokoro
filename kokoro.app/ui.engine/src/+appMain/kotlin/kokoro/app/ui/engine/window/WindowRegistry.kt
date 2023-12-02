package kokoro.app.ui.engine.window

fun interface WindowRegistry {

	fun newInstance(classFqn: String): WindowCore<*>
}
