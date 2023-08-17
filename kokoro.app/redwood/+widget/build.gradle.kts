import conv.sub.android.autoNamespace

plugins {
	id("kokoro.conv.kt.mpp.lib.sub") // MPP required by redwood generator plugin
	id("conv.redwood.gen.widget")
	id("conv.kt.mpp.assets")
	id("conv.redwood.ui.wv.setup")
}

val parent = project.parent!!

android {
	val client = parent.parent!!
	autoNamespace(project, client)
}

redwoodSchema {
	source.set(parent)
	type.set("kokoro.app.ui.wv.Schema")
}

dependencies {
	commonMainApi("app.cash.redwood:redwood-layout-widget")
}
