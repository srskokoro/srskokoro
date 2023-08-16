package conv.internal.setup

import app.cash.redwood.gradle.RedwoodComposeExtension
import app.cash.redwood.gradle.RedwoodGeneratorExtension
import app.cash.redwood.gradle.RedwoodSchemaExtension

internal fun setUp(redwood: RedwoodComposeExtension): Unit = with(redwood) {
	// Nothing (for now)
}

internal fun setUp(redwoodSchema: RedwoodGeneratorExtension): Unit = with(redwoodSchema) {
	setUp(this as RedwoodSchemaExtension)

	// Nothing (for now)
}

internal fun setUp(redwoodSchema: RedwoodSchemaExtension): Unit = with(redwoodSchema) {
	// Nothing (for now)
}
