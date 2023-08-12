import conv.internal.setup.*

plugins {
	id("app.cash.redwood.generator.modifiers")
}

redwoodSchema {
	setUp(this)
}
