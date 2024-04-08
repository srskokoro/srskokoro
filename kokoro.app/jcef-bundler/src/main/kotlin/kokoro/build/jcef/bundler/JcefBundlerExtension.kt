package kokoro.build.jcef.bundler

import kokoro.build.jcef.JcefExtension
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.kotlin.dsl.*
import java.io.File
import javax.inject.Inject

abstract class JcefBundlerExtension @Inject constructor(
	project: Project,
	objects: ObjectFactory,
) : JcefExtension(project, objects) {

	@get:Internal
	val natives: Provider<Directory>

	@get:Internal
	val nativeDependencyJar: Provider<File>

	init {
		val installJcefInputs by project.configurations.creating
		project.dependencies { installJcefInputs(nativeDependency) }
		nativeDependencyJar = installJcefInputs.incoming.artifacts.resolvedArtifacts.map { it.single().file }

		@Suppress("LeakingThis")
		val installJcef = project.tasks.register("installJcef", JcefBundlerTask::class.java, this)
		installJcef.configure { outputDir = this.project.layout.buildDirectory.dir("jcef") }
		natives = installJcef.flatMap { it.outputDir }
	}
}
