import conv.internal.setup.*

plugins {
	id("app.cash.redwood.schema")
}

redwoodSchema {
	setUp(this)
}
