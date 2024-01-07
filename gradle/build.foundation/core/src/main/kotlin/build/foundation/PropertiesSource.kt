package build.foundation

import org.gradle.api.Describable
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.util.Properties

// See also, https://github.com/JetBrains/kotlin/blob/v1.9.22/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/plugin/internal/CustomPropertiesFileValueSource.kt
internal abstract class PropertiesSource : ValueSource<Properties, PropertiesSource.Parameters>, Describable {

	interface Parameters : ValueSourceParameters {
		val from: RegularFileProperty
	}

	override fun getDisplayName(): String = "properties file '${parameters.from.get().asFile}'"

	override fun obtain() = Properties().also { out ->
		val from = parameters.from.get().asFile
		if (from.isFile) from.bufferedReader().use {
			out.load(it)
		}
	}
}
