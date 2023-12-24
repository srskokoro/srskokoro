package build.dotbuild

import build.api.SettingsPlugin

class _plugin : SettingsPlugin({
	gradle.allprojects {
		layout.buildDirectory.set(file(".build"))
	}
})
