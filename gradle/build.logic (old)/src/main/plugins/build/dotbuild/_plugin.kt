package build.dotbuild

import build.api.SettingsPlugin
import org.gradle.api.initialization.Settings

class _plugin : SettingsPlugin {
	override fun Settings.applyPlugin() {
		gradle.allprojects {
			layout.buildDirectory.set(file(".build"))
		}
	}
}
