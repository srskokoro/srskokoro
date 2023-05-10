package kokoro.app.ui

import javax.swing.JOptionPane

@JvmInline
actual value class AlertStyle private constructor(actual val value: Int) {

	actual override fun toString() = `-toString`(when (value) {
		JOptionPane.PLAIN_MESSAGE -> ::PLAIN.name
		JOptionPane.ERROR_MESSAGE -> ::ERROR.name
		JOptionPane.WARNING_MESSAGE -> ::WARN.name
		JOptionPane.QUESTION_MESSAGE -> ::QUESTION.name
		JOptionPane.INFORMATION_MESSAGE -> ::INFO.name
		else -> throw AssertionError("Unexpected: $value")
	})

	actual companion object {
		actual val PLAIN = AlertStyle(JOptionPane.PLAIN_MESSAGE)
		actual val ERROR = AlertStyle(JOptionPane.ERROR_MESSAGE)
		actual val WARN = AlertStyle(JOptionPane.WARNING_MESSAGE)
		actual val QUESTION = AlertStyle(JOptionPane.QUESTION_MESSAGE)
		actual val INFO = AlertStyle(JOptionPane.INFORMATION_MESSAGE)
	}
}
