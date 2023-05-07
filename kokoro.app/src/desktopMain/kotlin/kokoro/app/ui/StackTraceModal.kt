package kokoro.app.ui

import kokoro.app.AppBuild
import kokoro.app.ui.StackTraceModalImpl.HEADLESS_NOTICE_FATAL_ERROR_ENCOUNTERED_TERMINATING
import kokoro.app.ui.StackTraceModalImpl.NONZERO_STATUS
import kokoro.app.ui.StackTraceModalImpl.awaitDismiss
import kokoro.internal.anyError
import kokoro.internal.assert
import kokoro.internal.getSafeStackTrace
import kokoro.internal.io.UnsafeCharArrayWriter
import kokoro.internal.printSafeStackTrace
import kokoro.internal.ui.ensureBounded
import java.awt.Component
import java.awt.Dialog
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.InvocationEvent
import java.util.LinkedList
import javax.swing.AbstractAction
import javax.swing.GroupLayout
import javax.swing.GroupLayout.DEFAULT_SIZE
import javax.swing.GroupLayout.PREFERRED_SIZE
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.WindowConstants
import kotlin.system.exitProcess

object StackTraceModal {
	/**
	 * Eventually, prints the stacktrace of the given [throwable] to the
	 * standard error stream, then displays a [modal window](https://en.wikipedia.org/wiki/Modal_window)
	 * that provides the same information. The modal window will also give the
	 * user an option to quit the application immediately; quiting the
	 * application this way will cause the current process to [exit][kotlin.system.exitProcess]
	 * with a nonzero status code.
	 *
	 * If the [throwable] is an [Error] or contains an [Error] (as one of the
	 * exceptions in the stacktrace), then the only option given to the user is
	 * to quit the application now.
	 *
	 * ### Implementation notes
	 *
	 * The user will not be able to interact with any other window of the
	 * application until the modal window is dismissed. (This is achieved using
	 * [Dialog.ModalityType.TOOLKIT_MODAL] &ndash; the application is expected
	 * to not spawn any other window that uses the same trick.) Also, further
	 * calls to this method will not spawn another modal window until the
	 * current one is dismissed &ndash; i.e., further modal windows that would
	 * otherwise be spawned by this method will instead be queued and will not
	 * be displayed until the last one displayed is dismissed.
	 *
	 * If for some reason, the stacktrace could not be printed or the modal
	 * window could not be displayed, likely due to an internal failure, then
	 * the stacktrace for that is printed instead (to the standard error
	 * stream), before forcibly exiting the current process with a nonzero
	 * status code &ndash; i.e., this should instead be treated as an
	 * irrevocable, fatal error.
	 */
	fun print(throwable: Throwable) {
		Toolkit.getDefaultToolkit().systemEventQueue
			.postEvent(StackTraceModalEvent(throwable))
	}
}

@Suppress("NOTHING_TO_INLINE")
private inline fun echoStackTrace(stackTrace: String) {
	System.err.print(stackTrace)
	// ^ Deliberately not `println` as the stacktrace string is expected to have
	// a final newline.
}

private class StackTraceModalEvent(target: Throwable) : InvocationEvent(StackTraceModal, null) {
	private val deferredFailures = LinkedList<Throwable?>()
	private val stackTrace = target.getSafeStackTrace(deferredFailures::addLast)
	private val gotError = target.anyError()

	private companion object {
		private const val INTERNAL_FAILURE_CAPTION = "FATAL! STACKTRACE MODAL FAILED:"

		private var mostRecent: StackTraceModalEvent? = null
	}

	private var next: StackTraceModalEvent? = null

	override fun dispatch() {
		val prev = mostRecent
		mostRecent = this
		if (prev == null) try {
			var current = this
			outer@ while (true) {
				var stackTrace = current.stackTrace
				var gotError = current.gotError

				while (true) {
					echoStackTrace(stackTrace)

					if (!GraphicsEnvironment.isHeadless()) {
						awaitDismiss(stackTrace, gotError)
					} else if (gotError) {
						System.err.println(HEADLESS_NOTICE_FATAL_ERROR_ENCOUNTERED_TERMINATING)
						exitProcess(NONZERO_STATUS)
					} else {
						current.deferredFailures.addLast(null)
						// ^ Avoids a potential endless loop that may happen due
						// to the following code below.
					}

					val deferredFailure = current.deferredFailures.poll()
					if (deferredFailure == null) {
						current = current.next ?: return
						continue@outer
					}

					stackTrace = deferredFailure.getSafeStackTrace(current.deferredFailures::addLast)
					gotError = deferredFailure.anyError()
				}
			}
		} catch (ex: Throwable) {
			try {
				echoStackTrace(UnsafeCharArrayWriter().run {
					write(INTERNAL_FAILURE_CAPTION); writeln()
					ex.printSafeStackTrace(this)
					toString()
				})
			} finally {
				exitProcess(NONZERO_STATUS)
			}
		} else {
			prev.next = this
		}
	}
}

private object StackTraceModalImpl {
	const val NONZERO_STATUS = 1

	// --

	private const val TITLE_SUFFIX_UNHANDLED_EXCEPTION = " - Unhandled exception!"
	private const val TITLE_SUFFIX_FATAL_ERROR = " - Fatal error!"

	private const val LABEL_UNEXPECTED_ERROR_ENCOUNTERED = "An unexpected error was encountered:"
	private const val TEXT_FATAL_ERROR_ENCOUNTERED = "A fatal error was encountered, as one of the exceptions in the stacktrace."
	const val HEADLESS_NOTICE_FATAL_ERROR_ENCOUNTERED_TERMINATING = "" +
		"FATAL! `Error` encountered (as one of the exceptions in the stacktrace).\n" +
		"The app is now likely unstable.\n" +
		"Process terminating..."

	private const val SUGGEST_MAY_QUIT_NOW = "While the error can be ignored, you may also choose to quit the app now."
	private const val SUGGEST_MUST_QUIT_NOW = "The app is now likely unstable. It is recommended to quit the app now."

	// --

	private const val QUIT_NOW = "Quit now"
	private const val IGNORE = "Ignore"

	fun awaitDismiss(stackTrace: String, gotError: Boolean) {
		ensureAppLaf()

		val content = JPanel()
		val layout = GroupLayout(content)
		content.layout = layout

		layout.autoCreateGaps = true
		val hg = layout.createParallelGroup(GroupLayout.Alignment.LEADING, true)
		val vg = layout.createSequentialGroup()

		fun vAdd(component: Component, min: Int, max: Int) {
			hg.addComponent(component, 0, DEFAULT_SIZE, Int.MAX_VALUE)
			vg.addComponent(component, min, DEFAULT_SIZE, max)
		}

		JLabel(LABEL_UNEXPECTED_ERROR_ENCOUNTERED).apply {
			vAdd(this, min = PREFERRED_SIZE, max = PREFERRED_SIZE)
		}

		JScrollPane(JTextArea(stackTrace).apply {
			tabSize = 4
			font = font.run { Font(Font.MONOSPACED, style, size) }
			isEditable = false
		}).apply {
			vAdd(this, min = DEFAULT_SIZE, max = Int.MAX_VALUE)
		}

		val options: Array<Any>
		val defaultOption: Any
		val titleSuffix: String

		val closingSuggestion = if (!gotError) {
			options = arrayOf(QUIT_NOW, IGNORE)
			defaultOption = IGNORE
			titleSuffix = TITLE_SUFFIX_UNHANDLED_EXCEPTION

			SUGGEST_MAY_QUIT_NOW
		} else {
			options = arrayOf(QUIT_NOW)
			defaultOption = QUIT_NOW
			titleSuffix = TITLE_SUFFIX_FATAL_ERROR

			JLabel(TEXT_FATAL_ERROR_ENCOUNTERED).apply {
				vAdd(this, min = PREFERRED_SIZE, max = PREFERRED_SIZE)
			}
			SUGGEST_MUST_QUIT_NOW
		}

		JLabel(closingSuggestion).apply {
			vAdd(this, min = PREFERRED_SIZE, max = PREFERRED_SIZE)
		}

		layout.setHorizontalGroup(hg)
		layout.setVerticalGroup(vg)

		val pane = JOptionPane(
			content, JOptionPane.ERROR_MESSAGE,
			JOptionPane.DEFAULT_OPTION, null,
			options, defaultOption,
		)

		val parent = BaseAppWindow.lastActive
		pane.componentOrientation = (parent ?: JOptionPane.getRootFrame()).componentOrientation

		// Necessary to prevent `Esc` key "close" action (which is otherwise
		// still enabled even if `defaultCloseOperation` is configured).
		pane.actionMap.put("close", object : AbstractAction("close") {
			// @formatter:off
			init { enabled = false }
			// @formatter:on
			override fun setEnabled(newValue: Boolean) = Unit
			override fun actionPerformed(e: ActionEvent) = Unit
		})

		// NOTE: Deliberately not parented to anyone, so that it gets its own
		// entry in the system taskbar.
		pane.createDialog(AppBuild.TITLE + titleSuffix).apply {
			defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
			modalityType = Dialog.ModalityType.TOOLKIT_MODAL

			ensureBounded(2)
			isResizable = true
			setLocationRelativeTo(parent)

			playSystemSound()
			isVisible = true
			dispose()
		}

		assert({
			"Either we don't have an `Error` or the only option given to the " +
				"user is to quit the application now."
		}) { !gotError || pane.value == QUIT_NOW }

		if (gotError || pane.value == QUIT_NOW) {
			// User requested to quit now
			exitProcess(NONZERO_STATUS)
		}
	}

	private fun playSystemSound() {
		// See, https://bugs.openjdk.org/browse/JDK-8149630
		val toolkit = Toolkit.getDefaultToolkit()

		// For Windows: Hand (stop/error) -- https://www.autohotkey.com/docs/v2/lib/MsgBox.htm#Group_2_Icon
		toolkit.getDesktopProperty("win.sound.hand").let {
			if (it is Runnable) {
				it.run() // Will play the system sound for us
				return // Done!
			}
		}

		// Fallback, and for other desktop platforms:
		toolkit.beep()
	}
}
