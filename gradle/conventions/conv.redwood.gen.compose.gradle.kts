import conv.internal.setup.*

plugins {
	id("app.cash.redwood.generator.compose")
}

redwood {
	setUp(this)
}

redwoodSchema {
	setUp(this)
}
