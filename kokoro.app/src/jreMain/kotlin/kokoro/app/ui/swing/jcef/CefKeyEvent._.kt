package kokoro.app.ui.swing.jcef

import org.cef.handler.CefKeyboardHandler.CefKeyEvent
import org.cef.misc.EventFlags.*
import java.awt.Component
import java.awt.event.KeyEvent
import javax.swing.KeyStroke
import org.cef.handler.CefKeyboardHandler.CefKeyEvent.EventType as CefKeyEventType

fun CefKeyEvent.toKeyEvent(source: Component): KeyEvent {
	// See, https://github.com/chromiumembedded/java-cef/blob/0b8e42e/native/CefBrowser_N.cpp#L1676
	val type = type
	val awtKeyCode: Int
	val awtChar: Char
	val awtId: Int
	when (type) {
		CefKeyEventType.KEYEVENT_CHAR -> {
			awtKeyCode = KeyEvent.VK_UNDEFINED
			awtChar = character
			awtId = KeyEvent.KEY_TYPED
		}
		CefKeyEventType.KEYEVENT_KEYUP -> {
			awtKeyCode = windows_key_code
			awtChar = KeyEvent.CHAR_UNDEFINED
			awtId = KeyEvent.KEY_RELEASED
		}
		else -> {
			awtKeyCode = windows_key_code
			awtChar = KeyEvent.CHAR_UNDEFINED
			awtId = KeyEvent.KEY_PRESSED
		}
	}
	return KeyEvent(
		source, awtId, System.currentTimeMillis(),
		getAwtModifiers(), awtKeyCode, awtChar,
	)
}

fun CefKeyEvent.toKeyStroke(): KeyStroke {
	val type = type
	return if (type == CefKeyEventType.KEYEVENT_CHAR) {
		KeyStroke.getKeyStroke(character)
	} else {
		KeyStroke.getKeyStroke(
			windows_key_code,
			getAwtModifiers(),
			type == CefKeyEventType.KEYEVENT_KEYUP,
		)
	}
}

fun CefKeyEvent.getAwtModifiers(): Int {
	val cef = modifiers
	var awt = 0
	// See, https://github.com/chromiumembedded/java-cef/blob/0b8e42e/native/CefBrowser_N.cpp#L46
	if (cef and EVENTFLAG_ALT_DOWN != 0)
		awt = awt or KeyEvent.ALT_DOWN_MASK
	if (cef and EVENTFLAG_LEFT_MOUSE_BUTTON != 0)
		awt = awt or KeyEvent.BUTTON1_DOWN_MASK
	if (cef and EVENTFLAG_MIDDLE_MOUSE_BUTTON != 0)
		awt = awt or KeyEvent.BUTTON2_DOWN_MASK
	if (cef and EVENTFLAG_RIGHT_MOUSE_BUTTON != 0)
		awt = awt or KeyEvent.BUTTON3_DOWN_MASK
	if (cef and EVENTFLAG_CONTROL_DOWN != 0)
		awt = awt or KeyEvent.CTRL_DOWN_MASK
	if (cef and EVENTFLAG_COMMAND_DOWN != 0)
		awt = awt or KeyEvent.META_DOWN_MASK
	if (cef and EVENTFLAG_SHIFT_DOWN != 0)
		awt = awt or KeyEvent.SHIFT_DOWN_MASK
	return awt
}
