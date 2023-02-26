package srs.kokoro.jcef

import me.friwi.jcefmaven.EnumPlatform
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

internal interface JcefConfig {
	val taskGroup: String
	val installTaskName: String

	val platform: EnumPlatform

	val outputDir: DirectoryProperty
}

internal abstract class JcefConfigImpl @Inject constructor(
	objectFactory: ObjectFactory,
	projectLayout: ProjectLayout,
) : JcefConfig {
	final override val taskGroup get() = "jcef"
	final override val installTaskName get() = "installJcef"
	final override val platform get() = jcefBuildPlatform

	final override val outputDir: DirectoryProperty = objectFactory.directoryProperty().convention(
		projectLayout.buildDirectory.dir("generated/$installTaskName")
	)
}
