package kokoro.internal.ui

import kokoro.internal.assertUnreachable
import java.awt.event.InvocationEvent
import java.lang.reflect.Field
import java.lang.reflect.InaccessibleObjectException

fun InvocationEvent.isLikelyFromSwing(): Boolean {
	val eventClass = javaClass
	val targetModuleName =
		if (eventClass !== InvocationEvent::class.java) {
			eventClass.module
		} else {
			// A null `runnable` is highly unlikely to be set by Swing or AWT,
			// since otherwise, `InvocationEvent.dispatch()` would throw NPE.
			(Reflect_InvocationEvent.runnable?.get(this) ?: return false)
				.javaClass.module
		}.name
	@Suppress("ReplaceCallWithBinaryOperator")
	return "java.desktop".equals(targetModuleName)
}

private object Reflect_InvocationEvent {
	@JvmField val runnable: Field?

	init {
		var field: Field?
		try {
			field = InvocationEvent::class.java.getDeclaredField("runnable")
		} catch (_: NoSuchFieldException) {
			assertUnreachable(lazyMessage = {
				"Field `runnable` no longer declared directly by `InvocationEvent`"
			})
			var superclass = InvocationEvent::class.java.superclass
			do {
				field = superclass.declaredFields.find { it.name == "runnable" }
				if (field != null) break
				superclass = superclass.superclass
			} while (superclass != null)
		}
		if (field != null) try {
			field.isAccessible = true
		} catch (ex: InaccessibleObjectException) {
			assertUnreachable(ex, lazyMessage = {
				"""
				Requires JVM args:
				--add-opens=java.desktop/java.awt.event=ALL-UNNAMED
				""".trimIndent()
			})
			field = null
		}
		runnable = field
	}
}
