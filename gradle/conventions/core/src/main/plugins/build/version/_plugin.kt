package build.version

import build.api.ProjectPlugin
import build.api.dsl.*
import org.gradle.initialization.SettingsLocation
import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.support.serviceOf

class _plugin : ProjectPlugin({
	val rootProject = rootProject

	val internalVersion = rootProject.xs().getOrAdd<InternalVersion>("--internal-version--") {
		val providers = rootProject.providers
		val isReleasing = providers.isReleasing
		val rootSettingsDir = rootProject.gradle.findRoot().serviceOf<SettingsLocation>().settingsDir

		providers.of(InternalVersionLoader::class.java) {
			val p = parameters
			p.rootSettingsDir.set(rootSettingsDir)
			p.releasing.set(isReleasing)
		}.get()
	}

	internalVersion.name?.let { versionName = it }
	versionCode = internalVersion.code
})
