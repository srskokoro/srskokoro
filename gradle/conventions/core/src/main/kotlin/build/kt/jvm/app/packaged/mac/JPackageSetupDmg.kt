package build.kt.jvm.app.packaged.mac

abstract class JPackageSetupDmg : JPackageSetupMacOsBaseTask() {
	final override val type: String get() = "dmg"
}
