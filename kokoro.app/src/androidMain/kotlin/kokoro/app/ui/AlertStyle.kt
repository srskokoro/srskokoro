package kokoro.app.ui

@JvmInline
actual value class AlertStyle private constructor(actual val value: Int) {

	actual override fun toString(): String = TODO("Not yet implemented")

	actual companion object {
		actual val PLAIN: AlertStyle get() = TODO("Not yet implemented")
		actual val ERROR: AlertStyle get() = TODO("Not yet implemented")
		actual val WARN: AlertStyle get() = TODO("Not yet implemented")
		actual val QUESTION: AlertStyle get() = TODO("Not yet implemented")
		actual val INFO: AlertStyle get() = TODO("Not yet implemented")
	}
}
