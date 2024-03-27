package build.kt.jvm.app.packaged.win

abstract class JPackageSetupExe : JPackageSetupWindowsBaseTask() {
	final override val type: String get() = "exe"
}
