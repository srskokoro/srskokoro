package kokoro.app.ui.wv.setup

import kokoro.app.ui.wv.setup.WvSetup.S
import org.gradle.api.tasks.bundling.Zip
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Not worth caching")
abstract class WvSetupExportTask : Zip() {
	companion object {
		const val DEFAULT_EXTENSION = "wv.zip"
	}

	init {
		@Suppress("LeakingThis") val self = this
		self.archiveExtension.set(DEFAULT_EXTENSION)
		WvSetup.includeJsInputs(self)
		self.include("**/*${S.D_WV_SPEC}")
		self.include("**/*${S.D_WV_BASE_LST}")
	}
}
