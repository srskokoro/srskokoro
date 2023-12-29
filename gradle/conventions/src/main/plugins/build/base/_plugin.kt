package build.base

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.base
import build.api.dsl.accessors.baseOrNull
import build.support.from
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class _plugin : ProjectPlugin({
	apply {
		if (isRoot) plugin<build.root._plugin>()
		plugin("base")
		plugin<build.base.internal._plugin>()
	}

	prioritizedAfterEvaluate(fun Project.() {
		// Don't proceed if we're either a direct child of the root project or
		// the root project itself.
		if (depth <= 1) return
		val parent = parent ?: return

		// -=-
		// Prevents conflicts when generating the archives, e.g., for tasks that
		// output an installation distribution where all the archives are placed
		// under the same directory. See also, https://github.com/gradle/gradle/issues/847#issuecomment-1205001575
		val archivesNameProvider = provider {
			val parentBase = parent.baseOrNull
			if (parentBase != null) {
				parentBase.archivesName.map { "$it[.]$name" }
			} else provider {
				val path = path
				(if (path.startsWith(':')) path.from(1) else path)
					.replace(":", "[.]")
			}
		}.flatMap { it }
		// ^ NOTE: Prior to the "[.]" string being used above, single characters
		// were also considered, such as the '!' and '$' character. The '!'
		// character seemed problematic, as it seems to confuse the IDE (i.e.,
		// when a path with a '!' character is displayed in the build output's
		// console, auto-linking may not work properly), since the '!' character
		// is often used for denoting the inside of an archive.
		//
		// There are other problematic characters as well, simply because the
		// IDE (or perhaps the kotlin compiler?) fails to escape them whenever
		// they're displayed via file URIs:
		// - '#' -- interpreted as a URI hash fragment.
		// - '%' -- special escape character used in URIs.
		// - '+' -- interpreted as a space character in URIs.
		//
		// The '$', '@' and '~' seemed fine (even when unescaped in file URIs)
		// but they look weird.

		base.archivesName.convention(archivesNameProvider)
	})

	configurations.configureEach {
		if (!isCanBeResolved) return@configureEach // Skip

		// Fail on transitive upgrade/downgrade of direct dependency versions
		failOnDirectDependencyVersionGotcha(this)
	}
})
