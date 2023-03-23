package kokoro.jcef

import org.gradle.api.plugins.ExtensionAware

abstract class JcefExtension internal constructor() : ExtensionAware {
	internal companion object {
		const val DEFAULT_TASK_GROUP = "jcef"
	}
}
