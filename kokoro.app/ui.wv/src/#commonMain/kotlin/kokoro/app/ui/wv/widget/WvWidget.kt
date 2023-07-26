package kokoro.app.ui.wv.widget

import app.cash.redwood.Modifier
import app.cash.redwood.widget.Widget
import kokoro.app.ui.wv.WV_ELEM_ID_SLOT_BITS
import kokoro.app.ui.wv.appendWvElemId
import kotlin.jvm.JvmField

abstract class WvWidget(private val factory: BaseWvWidgetFactory) : Widget<WvWidget> {
	@JvmField @PublishedApi internal var _elemId: Int = 0

	inline val isBound get() = _elemId != 0

	open fun preBind(commandOut: StringBuilder): Int {
		val widgetId = factory.widgetIdPool.obtainId()

		val elemId = widgetId shl WV_ELEM_ID_SLOT_BITS // Reserves some bits
		if (elemId ushr WV_ELEM_ID_SLOT_BITS != widgetId) {
			throw Error("Overflow: ID generation already exhausted")
		}

		if (_elemId != 0) throw IllegalStateException("Already bound")
		_elemId = elemId

		val commandOutMark = commandOut.length
		commandOut.append("C$(\"")
		appendWvElemId(commandOut, elemId)
		commandOut.append('"')
		try {
			// TODO Bind construction/update arguments
			//  - Introduce each argument with a comma (',') prepended.
		} catch (ex: Throwable) {
			preBind_fail(commandOut, commandOutMark, ex)
		}
		commandOut.append(")")

		return elemId
	}

	private fun preBind_fail(commandOut: StringBuilder, commandOutMark: Int, ex: Throwable) {
		commandOut.setLength(commandOutMark) // Reset command

		val widgetId = _elemId ushr WV_ELEM_ID_SLOT_BITS
		factory.widgetIdPool.forceReverseObtainId(widgetId)
		_elemId = 0

		// TODO Somehow rethrow the caught exception at a later time after `commandOut` has been fed to the web view
	}

	open fun preUnbind(commandOut: StringBuilder) {
		val elemId = _elemId
		if (elemId == 0) throw IllegalStateException("Already unbound")
		_elemId = 0

		val widgetId = elemId ushr WV_ELEM_ID_SLOT_BITS
		factory.widgetIdPool.retireId(widgetId)

		commandOut.append("D$(\"")
		appendWvElemId(commandOut, elemId)
		commandOut.append("\")")
	}

	// --

	@Suppress("OVERRIDE_BY_INLINE")
	final override val value: WvWidget
		inline get() = this

	final override var modifier: Modifier = Modifier
}
