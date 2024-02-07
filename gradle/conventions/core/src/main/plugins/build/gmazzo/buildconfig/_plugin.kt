package build.gmazzo.buildconfig

import build.api.ProjectPlugin
import build.api.dsl.*
import com.github.gmazzo.buildconfig.BuildConfigExtension
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		plugin("com.github.gmazzo.buildconfig")
	}

	x<BuildConfigExtension>("buildConfig") {
		generateAtSync = true

		// The following makes sure that type-safe accessors for elements in
		// `sourceSets` are generated. See also, "Understanding when type-safe
		// model accessors are available | Gradle Kotlin DSL Primer | 8.5" --
		// https://docs.gradle.org/8.5/userguide/kotlin_dsl.html#kotdsl:accessor_applicability
		xs().add(typeOf(), "sourceSets", sourceSets)
	}
})
