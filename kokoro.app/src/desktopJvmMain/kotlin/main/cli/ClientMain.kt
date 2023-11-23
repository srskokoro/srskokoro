package main.cli

import com.github.ajalt.clikt.core.subcommands
import kokoro.app.AppBuild
import kokoro.internal.TODO
import main.cli.engine.BaseMain

internal open class ClientMain : BaseMain(
	name = System.getProperty("org.gradle.appname") ?: AppBuild.EXE_NAME,
) {
	init {
		subcommands(
			Open(),
		)
	}

	override suspend fun execute() {
		if (currentContext.invokedSubcommand != null) return
		TODO { IMPLEMENT("Display 'collection selection' window") }
	}
}
