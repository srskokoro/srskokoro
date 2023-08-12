import conv.internal.setup.*

plugins {
	id("app.cash.redwood.generator.widget")
}

redwoodSchema {
	setUp(this)
}
