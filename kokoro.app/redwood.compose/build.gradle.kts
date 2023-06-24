plugins {
	id("kokoro.conv.kt.mpp.lib.sub") // MPP required by redwood generator plugin
	id("app.cash.redwood.generator.compose")
}

val parent = project.parent!!

redwoodSchema {
	source.set(parent.project("redwood"))
	type.set("kokoro.app.ui.Schema")
}

dependencies {
	commonMainApi("app.cash.redwood:redwood-layout-compose")
	commonMainImplementation(parent.project("redwood.widget"))
}
