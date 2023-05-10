package kokoro.app.ui

import kotlin.jvm.JvmInline

@JvmInline
expect value class AlertStyle private constructor(val value: Int) {

	override fun toString(): String

	companion object {
		val PLAIN: AlertStyle
		val ERROR: AlertStyle
		val WARN: AlertStyle
		val QUESTION: AlertStyle
		val INFO: AlertStyle
	}
}

@Suppress("UnusedReceiverParameter")
internal inline fun AlertStyle.`-toString`(name: String) = "$`-AlertStyle-`($name)"
private const val `-AlertStyle-` = "AlertStyle"
