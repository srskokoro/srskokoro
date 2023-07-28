import conv.sub.android.autoNamespace

plugins {
	id("kokoro.conv.kt.mpp.lib.sub") // MPP required by redwood generator plugin
	id("app.cash.redwood.generator.widget")
	id("conv.kt.mpp.assets")
	id("conv.redwood.ui.wv.setup")
}

val parent = project.parent!!

android {
	val client = parent.parent!!
	autoNamespace(client)
}

redwoodSchema {
	source.set(parent)
	type.set("kokoro.app.ui.wv.Schema")
}

dependencies {
	commonMainApi("app.cash.redwood:redwood-layout-widget")
}
