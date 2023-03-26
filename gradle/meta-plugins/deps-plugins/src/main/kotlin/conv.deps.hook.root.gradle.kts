allprojects {
	configurations.all {
		// Provide version for each "direct" dependency that has no version
		dependencies.withType<ExternalDependency> {
			version {
				if (requiredVersion.isEmpty() && strictVersion.isEmpty()) {
					(deps.modules[group to name] ?: deps.moduleGroups[group])?.let { v ->
						val rejectedVersionsBackup = rejectedVersions
							.takeUnless { it.isEmpty() }
							?.toTypedArray()

						require(v) // NOTE: Clears `rejectedVersions`

						rejectedVersionsBackup?.let {
							reject(*it)
						}
					}
				}
			}
		}
	}
}
