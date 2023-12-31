package build.conventions.internal

import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

@InternalConventionsApi
object InternalConventions {

	fun markOrFail(project: Project) {
		// NOTE: Extension named like this to discourage direct access.
		project.extensions.add<Any>("--InternalConventions--", InternalConventions)
	}
}
