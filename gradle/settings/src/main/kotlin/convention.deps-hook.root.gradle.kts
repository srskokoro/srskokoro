deps.init()
allprojects {
	configurations.all {
		resolutionStrategy.eachDependency {
			requested.run {
				if (version.isNullOrEmpty()) {
					deps.modules[group to name]?.let {
						useVersion(it)
					}
				}
			}
		}
	}
}
