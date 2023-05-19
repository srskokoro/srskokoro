package kokoro.app.cli

import TODO
import com.github.ajalt.clikt.core.Abort
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.PrintCompletionMessage
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.CliktConsole
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
	private var _main: Main? = null
	val main: Main
		get() = _main ?: (currentContext.findRoot().command as Main)
			.also { _main = it }

	val workingDir: String
		get() = deferredState.workingDir

	private inline val deferredState
		get() = currentContext.console as DeferredState

	@Deprecated("Should not be called directly", ReplaceWith(""), DeprecationLevel.ERROR)
	final override fun run() = deferredState.pendingExecutions.addLast(this)

	// --

	protected open suspend fun CoroutineScope.execute() = Unit

	suspend fun feed(workingDir: String, args: Array<out String>, executionScope: CoroutineScope) {
		assertThreadSwing()

		val console = DeferredState(workingDir)
		context { this.console = console }

		try {
			parse(args.asList())
		} catch (ex: ProgramResult) {
			if (ex.statusCode != 0)
				echo("Error! Status code: ${ex.statusCode}", err = true)
			return // Exit
		} catch (ex: PrintHelpMessage) {
			echo(ex.command.getFormattedHelp(), err = ex.error)
			return // Exit
		} catch (ex: PrintCompletionMessage) {
			throw ex
		} catch (ex: PrintMessage) {
			echo(ex.message, err = ex.error)
			return // Exit
		} catch (ex: UsageError) {
			echo(ex.helpMessage(), err = true)
			when (ex.statusCode) {
				0, 1 -> {}
				else -> {
					val ls = currentContext.console.lineSeparator
					echo("$ls${ls}Status code: ${ex.statusCode}")
				}
			}
			return // Exit
		} catch (ex: CliktError) {
			echo(ex.message, err = true)
			return // Exit
		} catch (ex: Abort) {
			echo(currentContext.localization.aborted(), err = true)
			return // Exit
		} finally {
			try {
				console.consumeMessages()
			} catch (ex: Throwable) {
				throw AssertionError("Shouldn't fail", ex)
			}
		}

		for (cmd in console.pendingExecutions) with(cmd) {
			executionScope.execute()
		}
	}

	private class DeferredState(
		val workingDir: String,
	) : CliktConsole {
		var err: StringBuilder? = StringBuilder()
		var out: StringBuilder? = StringBuilder()

		val pendingExecutions = LinkedList<BaseCommand>()

		override fun promptForLine(prompt: String, hideInput: Boolean): String? = null

		override fun print(text: String, error: Boolean) {
			(if (error) err else out)!!.append(text)
		}

		override val lineSeparator = "\n"

		fun consumeMessages() {
			val errorMessage = err!!.trim(); err = null
			if (errorMessage.isNotEmpty()) {
				TODO // TODO Display dialog and wait for dismissal
			}

			val printMessage = out!!.trim(); out = null
			if (printMessage.isNotEmpty()) {
				TODO // TODO Display dialog and wait for dismissal
			}
		}
	}
}
