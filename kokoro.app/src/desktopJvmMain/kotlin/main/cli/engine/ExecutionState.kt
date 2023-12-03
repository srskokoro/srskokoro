package main.cli.engine

import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.PrintRequest
import com.github.ajalt.mordant.terminal.TerminalInfo
import com.github.ajalt.mordant.terminal.TerminalInterface
import kokoro.app.ui.swingAlert
import kokoro.internal.ui.assertThreadSwing
import java.util.LinkedList

internal class ExecutionState(
	val main: BaseMain,
	val workingDir: String,
	val args: Array<out String>,
) : TerminalInterface {
	val pendingExecutions = LinkedList<BaseCommand>()

	/** @see com.github.ajalt.mordant.terminal.TerminalRecorder */
	override val info = TerminalInfo(
		width = 79,
		height = 24,
		ansiLevel = AnsiLevel.NONE,
		ansiHyperLinks = false,
		outputInteractive = false,
		inputInteractive = false,
		crClearsLine = false,
	)

	var statusCode: Int? = null

	var hasError = false
		private set

	private val output = StringBuilder()

	override fun completePrintRequest(request: PrintRequest) {
		hasError = hasError or request.stderr
		val sb = output
		sb.append(request.text)
		if (request.trailingLinebreak) {
			sb.append("\n")
		}
	}

	override fun readLineOrNull(hideInput: Boolean): String? = null

	fun consumeMessages(): CharSequence {
		val sb = output
		val m = sb.trim(); sb.clear()
		return m
	}

	inline fun <T> consumeMessages(block: ExecutionState.(CharSequence) -> T): T {
		return block(consumeMessages())
	}

	fun consumeMessagesViaSwing() {
		assertThreadSwing()
		val m = consumeMessages()
		if (m.isNotEmpty()) {
			swingAlert({
				statusCode?.let { title = "Status Code: $it" }
				// TODO Output in a selectable monospaced text area instead.
				//  - NOTE: The `message` field here accepts Swing components.
				message = m
				if (hasError) style { ERROR }
			})
		}
	}

	suspend fun transition() {
		consumeMessagesViaSwing()
		for (cmd in pendingExecutions) {
			cmd.execute()
		}
	}
}
