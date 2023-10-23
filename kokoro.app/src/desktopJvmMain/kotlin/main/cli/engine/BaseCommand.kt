package main.cli.engine

import com.github.ajalt.clikt.core.CliktCommand

/**
 * @see CliktCommand
 */
internal abstract class BaseCommand(
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
	internal val execState get() = currentContext.obj as ExecutionState

	inline val main: BaseMain get() = execState.main

	inline val workingDir: String get() = execState.workingDir

	override fun run() = execState.pendingExecutions.addLast(this)

	open suspend fun execute() = Unit
}
