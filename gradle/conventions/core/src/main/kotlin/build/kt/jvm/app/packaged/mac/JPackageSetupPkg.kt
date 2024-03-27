package build.kt.jvm.app.packaged.mac

abstract class JPackageSetupPkg : JPackageSetupMacOsBaseTask() {
	final override val type: String get() = "pkg"
}
