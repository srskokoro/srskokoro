package build.kt.jvm.app.packaged.win

import build.api.file.file
import build.kt.jvm.app.packaged.JPackageSetupBaseTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.process.ExecSpec
import org.gradle.work.DisableCachingByDefault
import java.io.File

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

	// --

	@get:Optional
	@get:InputDirectory
	abstract val wixBinaries: DirectoryProperty

	override fun ExecSpec.jpackageConfigure() {
		val wixBinaries = wixBinaries.file
		if (File(wixBinaries, "candle.exe").isFile) {
			environment("PATH", wixBinaries.path + File.pathSeparatorChar +
				environment["PATH"])
		}
	}
}
