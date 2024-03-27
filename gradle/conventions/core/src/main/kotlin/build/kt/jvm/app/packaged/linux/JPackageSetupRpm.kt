package build.kt.jvm.app.packaged.linux

abstract class JPackageSetupRpm : JPackageSetupLinuxBaseTask() {
	final override val type: String get() = "rpm"
}
