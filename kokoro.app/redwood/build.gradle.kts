plugins {
	id("conv.kt.jvm") // JVM required by redwood schema plugin
	id("conv.redwood.schema")
}

redwoodSchema {
	type.set("kokoro.app.ui.wv.Schema")
}

dependencies {
	api("app.cash.redwood:redwood-layout-schema")
}
