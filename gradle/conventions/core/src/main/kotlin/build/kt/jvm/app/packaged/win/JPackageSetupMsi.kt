package build.kt.jvm.app.packaged.win

abstract class JPackageSetupMsi : JPackageSetupWindowsBaseTask() {
	final override val type: String get() = "msi"
}
