package kokoro.app.ui

import kokoro.app.AppBuild

//region

object Alerts

suspend inline fun Alerts.await(spec: AlertSpec.() -> Unit) = await(AlertHandler.DEFAULT, spec)

suspend inline fun Alerts.await(spec: AlertSpec) = await(AlertHandler.DEFAULT, spec)

suspend inline fun Alerts.await(handler: AlertHandler, spec: AlertSpec.() -> Unit) = await(handler, AlertSpec().apply(spec))

expect suspend fun Alerts.await(handler: AlertHandler, spec: AlertSpec): AlertButton?

//endregion

//region Callbacks

fun interface AlertHandler {

	fun onShow(token: AlertToken)

	companion object {
		val DEFAULT = AlertHandler { }
	}
}

interface AlertToken {

	fun dismiss(choice: AlertButton?)
}

@Suppress("NOTHING_TO_INLINE")
inline fun AlertToken.dismiss() = dismiss(null)

//endregion

//region Type-safe builder

@DslMarker
private annotation class AlertSpecDsl

@AlertSpecDsl
class AlertSpec {
	companion object {
		const val DEFAULT_TITLE = AppBuild.TITLE
	}

	var message: Any? = null
	var title: String = DEFAULT_TITLE
	var style: AlertStyle = AlertStyle.PLAIN

	var buttons: AlertButtonsSetup = AlertButtonsSetup.OK
	var defaultButton: Int = 0

	var isResizable = false

	/** @see kokoro.internal.ui.ensureBounded */
	var ensureBoundedByMaxDiv = 1

	@Suppress("NOTHING_TO_INLINE")
	inline fun init(
		message: Any? = null,
		title: String = DEFAULT_TITLE,
		style: AlertStyle = AlertStyle.PLAIN,

		buttons: AlertButtonsSetup = AlertButtonsSetup.OK,
		defaultButton: Int = 0,
	) {
		this.message = message
		this.title = title
		this.style = style

		this.buttons = buttons
		this.defaultButton = defaultButton
	}

	inline fun style(block: AlertStyles.() -> AlertStyle) {
		style = block(AlertStyles)
	}

	inline fun buttons(block: AlertButtonsSetup.Companion.() -> AlertButtonsSetup) {
		buttons = block(AlertButtonsSetup)
	}
}

//endregion

//region Style

@AlertSpecDsl
object AlertStyles {
	inline val PLAIN get() = AlertStyle.PLAIN
	inline val ERROR get() = AlertStyle.ERROR
	inline val WARN get() = AlertStyle.WARN
	inline val QUESTION get() = AlertStyle.QUESTION
	inline val INFO get() = AlertStyle.INFO
}

expect enum class AlertStyle {
	PLAIN,
	ERROR,
	WARN,
	QUESTION,
	INFO,
}

//endregion

//region Buttons

expect sealed interface AlertButton {
	val choice: AlertChoice
	val textOverride: Any?
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun AlertButton.invoke(textOverride: Any?): AlertButton {
	if (textOverride != null) {
		return AlertButtonOverride(choice, textOverride)
	}
	return this
}

@AlertSpecDsl
object AlertChoices {
	inline val OK get() = AlertChoice.OK
	inline val Cancel get() = AlertChoice.Cancel
	inline val Yes get() = AlertChoice.Yes
	inline val No get() = AlertChoice.No
}

expect enum class AlertChoice : AlertButton {
	OK,
	Cancel,
	Yes,
	No,
	;

	override val choice: AlertChoice
	override val textOverride: Nothing?
}

data class AlertButtonOverride(
	override val choice: AlertChoice,
	override val textOverride: Any,
) : AlertButton

// --

@Suppress("NOTHING_TO_INLINE")
inline fun AlertButtonsSetup(vararg buttons: AlertButton) = AlertButtonsSetup(isNonCancellable = false, buttons)

@Suppress("NOTHING_TO_INLINE")
inline fun AlertButtonsSetup(isNonCancellable: Boolean, vararg buttons: AlertButton) = AlertButtonsSetup(isNonCancellable, buttons)

@Suppress("NOTHING_TO_INLINE")
inline fun AlertButtonsSetup_NonCancellable(vararg buttons: AlertButton) = AlertButtonsSetup(isNonCancellable = true, buttons)

class AlertButtonsSetup(
	val isNonCancellable: Boolean,
	@PublishedApi internal val buttons: Array<out AlertButton>,
) : AbstractList<AlertButton>(), RandomAccess {

	@AlertSpecDsl
	companion object {
		val OK = with(AlertChoices) { AlertButtonsSetup(OK) }
		val OK_Cancel = with(AlertChoices) { AlertButtonsSetup(OK, Cancel) }

		val Yes_No = with(AlertChoices) { AlertButtonsSetup_NonCancellable(Yes, No) }
		val Yes_No_Cancel = with(AlertChoices) { AlertButtonsSetup(Yes, No, Cancel) }
	}

	operator fun invoke(vararg textOverrides: Any?): AlertButtonsSetup {
		val buttons = buttons
		val textOverrides_size = textOverrides.size
		return AlertButtonsSetup(isNonCancellable, Array(buttons.size) { i ->
			val button = buttons[i]
			if (i < textOverrides_size) textOverrides[i]?.let {
				return@Array AlertButtonOverride(button.choice, it)
			}
			button
		})
	}

	//region `List` implementation
	override val size: Int get() = buttons.size
	override fun isEmpty() = buttons.isEmpty()
	override fun contains(element: AlertButton) = buttons.contains(element)
	override fun get(index: Int) = buttons[index]
	override fun indexOf(element: AlertButton) = buttons.indexOf(element)
	override fun lastIndexOf(element: AlertButton) = buttons.lastIndexOf(element)
	//endregion

	public override fun toArray(): Array<Any?> = buttons.run {
		val n = size; copyInto(arrayOfNulls(n), 0, 0, n)
	}

	@Suppress("NOTHING_TO_INLINE")
	inline fun toTypedArray(): Array<out AlertButton> = buttons.copyOf()

	inline fun <reified R> mapToTypedArray(transform: (AlertButton) -> R): Array<R> {
		val buttons = buttons
		return Array(buttons.size) { transform(buttons[it]) }
	}
}

//endregion
