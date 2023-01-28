@Suppress("ClassName", "MemberVisibilityCanBePrivate")
internal class deps_bundle_spec internal constructor() {
	val modulesMap = mutableMapOf<String, String>()
	val modulesSeq = modulesMap.asSequence().map { (module, version) ->
		if (version.isEmpty()) module else "$module:$version"
	}

	fun module(dependencyNotation: String) {
		val groupDelimiterIdx = dependencyNotation.indexOf(':')
		require(groupDelimiterIdx >= 0) {
			"Supplied `String` module notation \"$dependencyNotation\" is invalid. " +
				"Example notations: \"org.gradle:gradle-core:2.2\", \"org.mockito:mockito-core:1.9.5:javadoc\""
		}

		val nameDelimiterIdx = dependencyNotation.indexOf(':', groupDelimiterIdx + 1)

		val moduleNotation: String
		val version: String

		if (nameDelimiterIdx < 0) {
			moduleNotation = dependencyNotation
			version = ""
		} else {
			moduleNotation = dependencyNotation.substring(0, nameDelimiterIdx)
			version = dependencyNotation.substring(nameDelimiterIdx + 1)
		}

		return module(moduleNotation, version)
	}

	fun module(moduleNotation: String, version: String) {
		modulesMap[moduleNotation] = version
	}

	fun module(group: String, name: String, version: String) = module("$group:$name", version)
}
