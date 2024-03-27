package build.kt.jvm.app.packaged.mac

import build.kt.jvm.app.packaged.JPackageSetupBaseTask
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Abstract super-class, not to be instantiated directly")
abstract class JPackageSetupMacOsBaseTask : JPackageSetupBaseTask() {

	final override val iconResFileName: String get() = RES_ICON_MAC_FILE

	override fun initJPackageExecArgs() {
		super.initJPackageExecArgs()
		jpackageExecArgs.apply {
			val spec = spec
			args("--mac-package-identifier", spec.appNs.get())
			args("--mac-package-name", spec.appTitleShort.get())
		}
	}
}
