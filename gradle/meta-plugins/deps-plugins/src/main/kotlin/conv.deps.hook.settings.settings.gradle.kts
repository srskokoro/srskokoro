pluginManagement {
	// Provide version for each plugin that has no version
	resolutionStrategy.eachPlugin {
		requested.run {
			if (version.isNullOrEmpty()) {
				(deps.plugins[id.id] ?: deps.pluginGroups[id.namespace])?.let {
					useVersion(it)
				}
			}
		}
	}
}
