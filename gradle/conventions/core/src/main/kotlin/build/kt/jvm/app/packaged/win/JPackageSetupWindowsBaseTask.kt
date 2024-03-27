package build.kt.jvm.app.packaged.win

import build.kt.jvm.app.packaged.JPackageSetupBaseTask
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Abstract super-class, not to be instantiated directly")
abstract class JPackageSetupWindowsBaseTask : JPackageSetupBaseTask() {

	final override val iconResFileName: String get() = RES_ICON_WIN_FILE

	override fun initJPackageExecArgs() {
		super.initJPackageExecArgs()
		jpackageExecArgs.apply {
			val spec = spec
			args("--win-upgrade-uuid", spec.packageUuid.get().toString())

			args("--win-dir-chooser")
			args("--win-menu", "--win-menu-group", spec.appTitle.get())
			args("--win-shortcut", "--win-shortcut-prompt")
		}
	}
}
