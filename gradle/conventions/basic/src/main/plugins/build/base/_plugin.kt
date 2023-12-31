package build.base

import build.api.ProjectPlugin
import build.api.dsl.*
import build.api.dsl.accessors.base
import build.api.dsl.accessors.baseOrNull
import build.conventions.internal.InternalConventions
import build.conventions.internal.InternalConventionsApi
import build.conventions.internal.ensureReproducibleBuild
import build.support.from
import org.gradle.api.Project

class _plugin : ProjectPlugin({
	apply {
		plugin("base")
	}

	@OptIn(InternalConventionsApi::class)
	InternalConventions.ensureReproducibleBuild(this)

	prioritizedAfterEvaluate(fun Project.() {
		// Don't proceed if we're either a direct child of the root project or
		// the root project itself.
		if (depth <= 1) return

		// -=-
		// Prevents conflicts when generating the archives, e.g., for tasks that
		// output an installation distribution where all the archives are placed
		// under the same directory. See also, https://github.com/gradle/gradle/issues/847#issuecomment-1205001575
		val archivesNameProvider = provider {
			parent?.baseOrNull?.let { parentBase ->
				return@provider parentBase.archivesName.map { "$it[.]$name" }
			}
			provider {
				val path = path
				(if (path.startsWith(':')) path.from(1) else path)
					.replace(":", "[.]")
			}
		}.flatMap { it }
		// ^ NOTE: Prior to the "[.]" string being used above, single characters
		// were also considered, such as the '!' and '$' character. The '!'
		// character is probably problematic, since the '!' character is often
		// used for denoting the inside of an archive. Still, `JarURLConnection`
		// uses the "!/" sequence instead for referring to an entry in a JAR --
		// see, https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/JarURLConnection.html
		//
		// There are other problematic characters as well, simply because the
		// Kotlin Gradle Plugin (or perhaps the Kotlin compiler?) fails to
		// escape them whenever they're printed to the console via file URIs:
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
