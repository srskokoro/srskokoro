package conv.deps.internal.util

@Suppress("NOTHING_TO_INLINE")
internal inline fun indexOfModuleGroupDelimiter(isDependencyNotation: Boolean, notation: String): Int {
	val groupDelimiterIdx = notation.indexOf(':')
	require(groupDelimiterIdx >= 0) {
		"Supplied `String` module notation \"$notation\" is invalid. " +
			if (!isDependencyNotation) "Example notations: \"org.gradle:gradle-core\", \"org.mockito:mockito-core\""
			else "Example notations: \"org.gradle:gradle-core:2.2\", \"org.mockito:mockito-core:1.9.5:javadoc\""
	}
	return groupDelimiterIdx
}
