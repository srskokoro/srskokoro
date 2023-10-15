package kokoro.app.ui.wv.setup

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
		WvSetup.includeExportables(self)
	}
}
