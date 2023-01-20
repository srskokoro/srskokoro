package srs.kokoro.jcef

import me.friwi.jcefmaven.EnumPlatform
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

internal interface JcefConfig {
	val taskGroup: String
	val installTaskName: String

	val platform: EnumPlatform

	val outputDir: DirectoryProperty
	val installDirRel: Property<String>
	val installDir: Provider<Directory>
}

internal abstract class JcefConfigImpl @Inject constructor(
	objectFactory: ObjectFactory,
	projectLayout: ProjectLayout,
) : JcefConfig {
	@get:Internal final override val taskGroup get() = "jcef"
	@get:Internal final override val installTaskName get() = "installJcef"
	@get:Internal final override val platform get() = jcefBuildPlatform

	@get:OutputDirectory
	final override val outputDir: DirectoryProperty = objectFactory.directoryProperty().convention(
		projectLayout.buildDirectory.dir("generated/$installTaskName")
	)

	@get:Input
	final override val installDirRel = objectFactory.property<String>().convention(".")

	@get:Internal
	final override val installDir = outputDir.dir(installDirRel)
}
