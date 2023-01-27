@Suppress("ClassName", "MemberVisibilityCanBePrivate")
internal object deps_versions {
	val plugins = mutableMapOf<String, String>()
	val pluginGroups = mutableMapOf<String, String>()

	val modules = mutableMapOf<Pair<String, String>, String>()
	val moduleGroups = mutableMapOf<String, String>()

	fun plugin(id: String, version: String) {
		plugins[id] = version
	}

	fun pluginGroup(group: String, version: String) {
		pluginGroups[group] = version
	}

	fun module(moduleNotation: String, version: String) {
		val groupDelimiterIdx = moduleNotation.indexOf(':')
		require(groupDelimiterIdx >= 0) {
			"Supplied `String` module notation \"$moduleNotation\" is invalid. " +
				"Example notations: \"org.gradle:gradle-core\", \"org.mockito:mockito-core\""
		}

		return module(
			group = moduleNotation.substring(0, groupDelimiterIdx),
			name = moduleNotation.substring(groupDelimiterIdx + 1),
			version = version,
		)
	}

	fun module(moduleNotation: Pair<String, String>, version: String) {
		modules[moduleNotation] = version
	}

	fun module(group: String, name: String, version: String) = module(group to name, version)

	fun moduleGroup(group: String, version: String) {
		moduleGroups[group] = version
	}
}
