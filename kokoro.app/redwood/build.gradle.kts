plugins {
	id("conv.sub")
	id("conv.kt.jvm") // JVM required by redwood schema plugin
	id("app.cash.redwood.schema")
}

redwoodSchema {
	type.set("kokoro.app.ui.Schema")
}

dependencies {
	api("app.cash.redwood:redwood-layout-schema")
}
