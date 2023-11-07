package conv.internal.setup

import app.cash.redwood.gradle.RedwoodComposeExtension
import app.cash.redwood.gradle.RedwoodGeneratorExtension
import app.cash.redwood.gradle.RedwoodSchemaExtension
import org.gradle.api.Project
import resolve

internal fun Project.setUp(redwood: RedwoodComposeExtension): Unit = with(redwood) {
	deps?.let { deps ->
		deps.modules.resolve("org.jetbrains.compose.compiler:compiler")?.value?.let { version ->
			kotlinCompilerPlugin.set(version)
		}
	}
}

internal fun setUp(redwoodSchema: RedwoodGeneratorExtension): Unit = with(redwoodSchema) {
	setUp(this as RedwoodSchemaExtension)

	// Nothing (for now)
}

internal fun setUp(redwoodSchema: RedwoodSchemaExtension): Unit = with(redwoodSchema) {
	// Nothing (for now)
}
