package build.kt.jvm.app.packaged.linux

import build.kt.jvm.app.packaged.JPackageSetupBaseTask
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Abstract super-class, not to be instantiated directly")
abstract class JPackageSetupLinuxBaseTask : JPackageSetupBaseTask() {

	final override val iconResFileName: String get() = RES_ICON_LINUX_FILE

	override fun initJPackageExecArgs() {
		super.initJPackageExecArgs()
		jpackageExecArgs.apply {
			val spec = spec
			args("--linux-menu-group", spec.appTitle.get())
			args("--linux-shortcut")
		}
	}
}
