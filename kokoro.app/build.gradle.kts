plugins {
	id("kokoro.conv.kt.mpp.lib")
	id("conv.kt.mpp.targets")
	id("conv.kt.mpp.assets")
	id("conv.gmazzo.buildconfig")
	id("conv.version")
	id("conv.ktx.atomicfu")
	id("kokoro.app.ui.wv.setup")
	id("conv.redwood")
	kotlin("plugin.serialization")
}

kotlin {
	/**
	 * See 'build.targets.cf' to declare build targets, then use
	 * `targets.<targetName>` here to configure them further.
	 *
	 * See also [conv.internal.setup.setUpTargetsExtensions] in the convention
	 * plugins.
	 */
	@Suppress("UNUSED_VARIABLE") val eat_comment: Nothing

	targets.desktopJvm {
		// TODO Uncomment eventually to allow `.java` sources -- https://youtrack.jetbrains.com/issue/KT-30878
		//withJava()
	}
}

val NAMESPACE = extra["kokoro.app.ns"] as String
val APP_TITLE = extra["kokoro.app.title"] as String
val APP_TITLE_SHORT = extra["kokoro.app.title.short"] as String

android {
	namespace = NAMESPACE
	defaultConfig.manifestPlaceholders.let { map ->
		map[::APP_TITLE.name] = APP_TITLE
		map[::APP_TITLE_SHORT.name] = APP_TITLE_SHORT
	}
}

dependencies {
	commonMainApi(project("core.base"))
	commonMainApi(project("core.components"))
	commonMainApi(project("core.compose"))

	commonMainImplementation(project(":kokoro:internal"))
	appMainImplementation("androidx.annotation:annotation")

	appMainImplementation(project("redwood:compose"))
	appMainImplementation(project("redwood:widget"))
	project("ui.engine").let {
		commonMainImplementation(it)
		commonMainWvSetup(it)
	}
	project("ui.framework").let {
		commonMainImplementation(it)
		commonMainWvSetup(it)
	}

	appMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
	appMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json-okio")
	appMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor")

	appMainImplementation("cafe.adriel.voyager:voyager-navigator")

	appMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	desktopJvmMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing")

	androidMainApi("androidx.core:core-ktx")
	androidMainApi("androidx.activity:activity-ktx")
	androidMainApi("androidx.webkit:webkit")

	appMainImplementation("com.squareup.okio:okio")
	desktopJvmMainImplementation("net.harawata:appdirs")

	desktopJvmMainImplementation("com.github.ajalt.clikt:clikt")
}
