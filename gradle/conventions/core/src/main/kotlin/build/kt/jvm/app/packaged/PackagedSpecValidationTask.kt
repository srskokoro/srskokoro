package build.kt.jvm.app.packaged

import org.gradle.api.tasks.TaskAction

abstract class PackagedSpecValidationTask : PackagedSpecAbstractTask() {
	@TaskAction
	fun validate() {
		spec.validate(logger)
	}
}
