package kokoro.app.ui

abstract class AlertChoice {

	abstract fun getButton(context: AlertContext?): Any?

	abstract fun getText(context: AlertContext?): Any

	override fun toString(): String =
		getText(null).toString()
}

inline fun AlertChoice(
	crossinline lazyButton: AlertChoice.(AlertContext?) -> Any? = { getText(it) },
	crossinline lazyText: AlertChoice.(AlertContext?) -> Any,
): AlertChoice = object : AlertChoice() {
	override fun getButton(context: AlertContext?) = lazyButton(context)
	override fun getText(context: AlertContext?) = lazyText(context)
}

inline fun AlertChoice2(
	crossinline lazyButton: AlertChoice.(AlertContext?, text: Any) -> Any?,
	crossinline lazyText: AlertChoice.(AlertContext?) -> Any,
): AlertChoice = object : AlertChoice() {
	override fun getButton(context: AlertContext?) = lazyButton(context, getText(context))
	override fun getText(context: AlertContext?) = lazyText(context)
}
