pluginManagement {
	deps.init()
	resolutionStrategy.eachPlugin {
		requested.run {
			if (version.isNullOrEmpty()) {
				deps.plugins[id.id]?.let {
					useVersion(it)
				}
			}
		}
	}
}
