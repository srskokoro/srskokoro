package kokoro.app.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.PrintRequest
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.TerminalInfo
import com.github.ajalt.mordant.terminal.TerminalInterface
import kokoro.app.ui.Alerts
import kokoro.app.ui.swing
import kokoro.internal.ui.assertThreadSwing
import kotlinx.coroutines.CoroutineScope
import java.util.LinkedList

/**
 * @see CliktCommand
 */
abstract class BaseCommand(
	help: String = "",
	epilog: String = "",
	name: String? = null,
	invokeWithoutSubcommand: Boolean = false,
	printHelpOnEmptyArgs: Boolean = false,
	helpTags: Map<String, String> = emptyMap(),
	autoCompleteEnvvar: String? = "",
	allowMultipleSubcommands: Boolean = false,
	treatUnknownOptionsAsArgs: Boolean = false,
	hidden: Boolean = false,
) : CliktCommand(
	help = help,
	epilog = epilog,
	name = name,
	invokeWithoutSubcommand = invokeWithoutSubcommand,
	printHelpOnEmptyArgs = printHelpOnEmptyArgs,
	helpTags = helpTags,
	autoCompleteEnvvar = autoCompleteEnvvar,
	allowMultipleSubcommands = allowMultipleSubcommands,
	treatUnknownOptionsAsArgs = treatUnknownOptionsAsArgs,
	hidden = hidden,
) {
	val main: Main
		get() = execState.main!!

	val workingDir: String
		get() = execState.workingDir

	private inline val execState
		get() = currentContext.obj as ExecutionState

	@Deprecated("Should not be called directly", ReplaceWith(""), DeprecationLevel.ERROR)
	final override fun run() = execState.pendingExecutions.addLast(this)

	// --

	protected open suspend fun CoroutineScope.execute() = Unit

	suspend fun feed(workingDir: String, args: Array<out String>, executionScope: CoroutineScope) {
		assertThreadSwing()

		val execState = ExecutionState(this as? Main, workingDir)
		context {
			obj = execState
			// See, https://ajalt.github.io/clikt/advanced/#replacing-stdin-and-stdout
			terminal = Terminal(terminalInterface = execState)
		}

		try {
			parse(args.asList())
		} catch (ex: CliktError) {
			echoFormattedHelp(ex)
			echo()
			echo("EXIT CODE: ${ex.statusCode}")
			return // Exit
		} finally {
			execState.consumeMessages()
		}

		for (cmd in execState.pendingExecutions) with(cmd) {
			executionScope.execute()
		}
	}

	private class ExecutionState(
		var main: Main? = null,
		val workingDir: String,
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

		var hasError = false
		val output = StringBuilder()

		override fun completePrintRequest(request: PrintRequest) {
			hasError = hasError or request.stderr
			val sb = output
			sb.append(request.text)
			if (request.trailingLinebreak) {
				sb.append("\n")
			}
		}

		override fun readLineOrNull(hideInput: Boolean): String? = null

		fun consumeMessages() {
			val sb = output
			val m = sb.trim(); sb.clear()
			if (m.isNotEmpty()) try {
				Alerts.swing(null) {
					// TODO Output in a selectable monospaced text area instead.
					//  - NOTE: The `message` field here accepts Swing components.
					message = m
					if (hasError) style { ERROR }
				}
			} catch (ex: Throwable) {
				throw AssertionError("Shouldn't fail", ex)
			}
		}
	}
}
