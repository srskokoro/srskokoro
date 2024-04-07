package kokoro.build.jcef

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.kotlin.dsl.*
import javax.inject.Inject

abstract class JcefExtension @Inject constructor(
	project: Project,
	objects: ObjectFactory,
) {

	/**
	 * The “JCEF commit” tag (the Git commit hash from the [Java CEF repo](https://github.com/chromiumembedded/java-cef))
	 * indicated by the JCEF Maven release page – see, “[Releases · jcefmaven/jcefmaven · GitHub](https://github.com/jcefmaven/jcefmaven/releases)”.
	 */
	@get:Input
	val commit: Property<String> = objects.property()

	/**
	 * The “CEF version” tag indicated by the JCEF Maven release page – see, “[Releases · jcefmaven/jcefmaven · GitHub](https://github.com/jcefmaven/jcefmaven/releases)”.
	 */
	@get:Input
	val cef: Property<String> = objects.property()

	// --

	companion object {

		internal const val MVN_GROUP = "me.friwi"

		internal inline val PLATFORM_ID get() = JcefPlatform.ID
	}

	@get:Internal
	val dependencyVersion: Provider<String> =
		commit.zip(cef) { commit, cef -> "jcef-$commit+cef-$cef" }

	@get:Internal
	val dependency: Provider<String> =
		dependencyVersion.map { "$MVN_GROUP:jcef-api:$it!!" }

	@get:Internal
	internal val nativeDependency: Provider<String> =
		dependencyVersion.map { "$MVN_GROUP:jcef-natives-$PLATFORM_ID:$it!!" }

	@get:Internal
	internal val nativeDependencyTarResource: Provider<String> =
		// See, https://github.com/jcefmaven/jcefmaven/blob/122.1.10/jcefmaven/src/main/java/me/friwi/jcefmaven/impl/step/fetch/PackageClasspathStreamer.java#L15
		dependencyVersion.map { "jcef-natives-$PLATFORM_ID-$it.tar.gz" }

	//--

	init {
		project.deps {
			val props = props.map
			props["jcef.commit"]?.let { commit.convention(it) }
			props["jcef.cef"]?.let { cef.convention(it) }
		}
		project.afterEvaluate(fun Project.(): Unit = afterEvaluate(fun Project.() = afterEvaluate(fun(_) {
			dependency.get() // Force it to throw if not configured
		})))
	}

	@get:Internal
	val requiredJvmArgs get() = JcefPlatform.requiredJvmArgs
}
