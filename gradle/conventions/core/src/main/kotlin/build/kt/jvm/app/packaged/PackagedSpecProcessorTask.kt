package build.kt.jvm.app.packaged

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Nested
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Abstract super-class, not to be instantiated directly")
abstract class PackagedSpecProcessorTask : DefaultTask() {

	@get:Nested
	lateinit var spec: PackagedSpec
}
