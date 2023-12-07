package kokoro.app.ui

import androidx.compose.runtime.*
import kokoro.app.AppBuild
import kotlin.math.max

//#region

@Composable
inline fun Alert(
	spec: AlertSpec.() -> Unit,
	interceptor: AlertInterceptor = AlertInterceptor.DEFAULT,
	crossinline recipient: (AlertButton?) -> Unit = {},
) = Alert(AlertSpec().apply(spec), interceptor, recipient)

/**
 * ### Implementation notes
 *
 * Upon displaying the alert, changing the arguments given won't cause the alert
 * to be dismissed and/or re-displayed. It will only be dismissed by removing
 * this composable from the composition on next recompose. Once the alert is
 * dismissed, it can be re-displayed by re-introducing this composable to the
 * composition.
 */
@Composable
expect inline fun Alert(
	spec: AlertSpec,
	interceptor: AlertInterceptor = AlertInterceptor.DEFAULT,
	crossinline recipient: (AlertButton?) -> Unit = {},
)

//#endregion

//#region Callbacks

interface AlertInterceptor {

	fun onShow(token: AlertToken)

	companion object {
		val DEFAULT = AlertInterceptor { }
	}
}

inline fun AlertInterceptor(crossinline onShow: (token: AlertToken) -> Unit) = object : AlertInterceptor {
	override fun onShow(token: AlertToken) {
		onShow.invoke(token)
	}
}

interface AlertToken {

	fun dismiss(choice: AlertButton?)
}

@Suppress("NOTHING_TO_INLINE")
inline fun AlertToken.dismiss() = dismiss(null)

//#endregion

//#region Type-safe builder

@DslMarker
private annotation class AlertSpecDsl

@AlertSpecDsl
class AlertSpec {
	companion object {
		const val DEFAULT_TITLE_BASE = AppBuild.TITLE
	}

	var message: Any? = null

	/** @see titleBase */
	var title: String? = null

	var style: AlertStyle = AlertStyle.PLAIN

	var buttons: AlertButtonsSetup = AlertButtonsSetup.OK
	var defaultButton: Int = 0

	// --

	/**
	 * An extra string to accompany the provided [title] string. Depending on
	 * the platform, it may be prepended to the [title] (separated by an
	 * implementation-defined delimiter) or be displayed as an overline (a small
	 * text or caption) above the [title]. The implementation may even ignore
	 * this completely, unless [title] is `null`.
	 *
	 * If [title] is `null`, [titleBase] will be used instead. If [titleBase] is
	 * `null`, only [title] is used. By default, [titleBase] is non-null &ndash;
	 * its default value is [DEFAULT_TITLE_BASE].
	 */
	var titleBase: String? = DEFAULT_TITLE_BASE

	inline fun style(block: AlertStyles.() -> AlertStyle) {
		style = block(AlertStyles)
	}

	inline fun buttons(block: AlertButtonsSetup.Companion.() -> AlertButtonsSetup) {
		buttons = block(AlertButtonsSetup)
	}

	// --

	/** @see AlertSpec.resizableAndBounded */
	var isResizable = false

	/**
	 * NOTE: Applicable only if [isResizable] is `true`.
	 *
	 * @see kokoro.internal.ui.ensureBounded
	 * @see AlertSpec.resizableAndBounded
	 */
	var ensureBoundedByMaxDiv = 1

	/**
	 * @see AlertSpec.isResizable
	 * @see AlertSpec.ensureBoundedByMaxDiv
	 */
	@Suppress("NOTHING_TO_INLINE")
	inline fun resizableAndBounded(ensureBoundedByMaxDiv: Int = 2) {
		this.isResizable = true
		this.ensureBoundedByMaxDiv = ensureBoundedByMaxDiv
	}
}

//#endregion

//#region Style

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

//#endregion

//#region Buttons

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
	inline val CustomAction get() = AlertChoice.CustomAction
}

expect enum class AlertChoice : AlertButton {
	OK,
	Cancel,
	Yes,
	No,
	CustomAction,
	;

	override val choice: AlertChoice
	override val textOverride: Nothing?
}

data class AlertButtonOverride(
	override val choice: AlertChoice,
	override val textOverride: Any,
) : AlertButton

internal object AlertButtonImplCommon {
	const val TEXT_DEFAULT_CustomAction = "\u22EF" // Midline horizontal ellipsis
}

inline fun AlertButton?.matches(lazyValue: AlertChoices.() -> AlertChoice) = this?.choice == lazyValue(AlertChoices)

inline fun AlertChoice?.matches(lazyValue: AlertChoices.() -> AlertChoice) = this == lazyValue(AlertChoices)

inline fun <B : AlertButton> B?.ifChoiceMatches(lazyValue: AlertChoices.() -> AlertChoice, block: (B) -> Unit) {
	val value = lazyValue(AlertChoices)
	if (this?.choice == value) block(this)
}

inline fun AlertChoice?.ifMatches(lazyValue: AlertChoices.() -> AlertChoice, block: (AlertChoice) -> Unit) {
	val value = lazyValue(AlertChoices)
	if (this == value) block(this)
}

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

	@Suppress("NOTHING_TO_INLINE")
	inline operator fun invoke() = this

	operator fun invoke(vararg textOverrides: Any?): AlertButtonsSetup {
		val buttons = buttons
		val buttons_size = buttons.size
		val textOverrides_size = textOverrides.size
		return AlertButtonsSetup(isNonCancellable, Array(max(buttons_size, textOverrides_size)) { i ->
			val button = if (i < buttons_size) buttons[i] else AlertChoice.CustomAction
			if (i < textOverrides_size) {
				val override = textOverrides[i]
				if (override != null) {
					return@Array AlertButtonOverride(button.choice, override)
				}
			}
			button
		})
	}

	//#region `List` implementation
	override val size: Int get() = buttons.size
	override fun isEmpty() = buttons.isEmpty()
	override fun contains(element: AlertButton) = buttons.contains(element)
	override fun get(index: Int) = buttons[index]
	override fun indexOf(element: AlertButton) = buttons.indexOf(element)
	override fun lastIndexOf(element: AlertButton) = buttons.lastIndexOf(element)
	//#endregion

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

//#endregion