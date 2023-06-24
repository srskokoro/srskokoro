import conv.sub.android.autoNamespace

plugins {
	id("kokoro.conv.kt.mpp.lib.sub") // MPP required by redwood generator plugin
	id("app.cash.redwood.generator.widget")
}

val parent = project.parent!!
val client = parent.parent!!
android.autoNamespace(client)

redwoodSchema {
	source.set(parent)
	type.set("kokoro.app.ui.Schema")
}

dependencies {
	commonMainApi("app.cash.redwood:redwood-layout-widget")
}
