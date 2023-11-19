plugins {
	id("kokoro.conv.kt.mpp.lib")
	id("conv.kt.mpp.assets")
	id("conv.gmazzo.buildconfig")
}

val NAMESPACE = extra["kokoro.app.ns"] as String
val APP_TITLE = extra["kokoro.app.title"] as String
val APP_TITLE_SHORT = extra["kokoro.app.title.short"] as String

buildConfig.appMain {
	publicObject("AppBuild") inPackage NAMESPACE

	buildConfigField("String", "EXE_NAME", "\"${extra["kokoro.app.exe.name"]}\"")

	buildConfigField("String", "TITLE", "\"$APP_TITLE\"")
	buildConfigField("String", "TITLE_SHORT", "\"$APP_TITLE_SHORT\"")

	val parent = evaluatedParent
	buildConfigField("String", "VERSION", "\"${parent.version}\"")
	buildConfigField("int", "VERSION_CODE", "${parent.versionCode}")
	if (parent.versionCode == 0) throw InvalidUserDataException(
		"Version code 0 (zero) should not be used"
	)
}

buildConfig.desktopMain {
	publicObject("AppBuildDesktop") inPackage NAMESPACE
	buildConfigField("String", "APP_DATA_DIR_NAME", "\"SRSKokoro${if (isReleasing) "" else "-Dev"}\"")
	buildConfigField("String", "USER_COLLECTIONS_DIR_NAME", "\"SRS Kokoro${if (isReleasing) "" else " (Dev)"}\"")
}

dependencies {
	commonMainImplementation(project(":kokoro:internal"))

	appMainApi("com.squareup.okio:okio")
}
