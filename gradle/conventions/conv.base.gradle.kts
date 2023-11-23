import build.internal.support.from
import conv.internal.setup.*
import conv.util.*

plugins {
	base
}

tasks.withType<AbstractArchiveTask>().configureEach {
	// By default, don't include the version in the names of output archives.
	// - This clears the default behavior set by the `base` plugin.
	archiveVersion.convention(null as String?)

	// The following ensures that builds are reproducible.
	// - See, https://docs.gradle.org/8.2.1/userguide/working_with_files.html#sec:reproducible_archives
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
}

prioritizedAfterEvaluate(fun Project.() {
	// Don't proceed if we're either a direct child of the root project or the
	// root project itself.
	if (depth <= 1) return
	val parent = parent ?: return

	// NOTE: Blocks until the parent project is evaluated.
	evaluationDependsOn(parent.path)

	// -=-
	// Prevents conflicts when generating the archives, e.g., for tasks that
	// output an installation distribution where all the archives are placed
	// under the same directory. See also, https://github.com/gradle/gradle/issues/847#issuecomment-1205001575

	val parentBase = parent.baseExtOrNull
	val archivesNameProvider = if (parentBase != null) {
		parentBase.archivesName.map { "$it${'$'}$name" }
	} else provider {
		val path = path
		(if (path.startsWith(':')) path.from(1) else path)
			.replace(':', '$')
	}
	// ^ NOTE: Prior to the '$' character being used above, the '!' character
	// was also used in the past, but this seems to confuse the IDE (i.e., when
	// a path with a '!' character is displayed in the build output's console,
	// auto-linking may not work properly), since the '!' character is often
	// used for denoting the inside of an archive. Hence, the '$' is now being
	// used instead.

	base.archivesName.convention(archivesNameProvider)

	// -=-
	// Automatically derive a namespace for Android projects based on their
	// parent project.

	androidExtOrNull?.let { android ->
		if (android.namespace == null)
			android.autoNamespaceOrNop(project)
	}
})

configurations.configureEach {
	if (isCanBeResolved) {
		// Fail on transitive upgrade/downgrade of direct dependency versions
		failOnDirectDependencyVersionGotcha(gradle)
	}
}
