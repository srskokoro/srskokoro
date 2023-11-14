import conv.util.*

plugins {
	id("kokoro.conv.kt.mpp.lib") // MPP required by redwood generator plugin
	id("conv.redwood.gen.widget")
	id("conv.kt.mpp.assets")
}

val parent = evaluatedParent

android {
	val client = parent.evaluatedParent
	autoNamespace(project, client)
}

redwoodSchema {
	source.set(parent)
	type.set("kokoro.app.ui.wv.Schema")
}

dependencies {
	commonMainApi("app.cash.redwood:redwood-layout-widget")
}
