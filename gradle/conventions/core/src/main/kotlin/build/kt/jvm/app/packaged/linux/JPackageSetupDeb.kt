package build.kt.jvm.app.packaged.linux

abstract class JPackageSetupDeb : JPackageSetupLinuxBaseTask() {
	final override val type: String get() = "deb"
}
