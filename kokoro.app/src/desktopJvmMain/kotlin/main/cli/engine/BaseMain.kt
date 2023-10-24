package main.cli.engine

import com.github.ajalt.clikt.completion.CompletionCandidates
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.terminal.Terminal
import kokoro.app.AppBuildDesktop
import net.harawata.appdirs.AppDirsFactory

internal abstract class BaseMain : BaseCommand(invokeWithoutSubcommand = true) {

	val mainDataDir by option("-d", "--data", envvar = "SRS_KOKORO_DATA").convert(
		{ localization.pathMetavar() }, CompletionCandidates.Path,
	) { it }.defaultLazy {
		AppDirsFactory.getInstance()
			.getUserDataDir(AppBuildDesktop.APP_DATA_DIR_NAME, null, null, /* roaming = */false)
	}


	internal fun feed(workingDir: String, args: Array<out String>): ExecutionState {
		val execState = ExecutionState(this, workingDir, args)
		context {
			obj = execState
			// See, https://ajalt.github.io/clikt/advanced/#replacing-stdin-and-stdout
			terminal = Terminal(terminalInterface = execState)
		}
		try {
			parse(args.asList())
		} catch (ex: CliktError) {
			echoFormattedHelp(ex)
			execState.statusCode = ex.statusCode
			execState.pendingExecutions.clear()
		}
		return execState
	}
}
