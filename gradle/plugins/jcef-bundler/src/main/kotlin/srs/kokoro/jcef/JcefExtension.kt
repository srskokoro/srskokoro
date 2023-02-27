package srs.kokoro.jcef

import org.gradle.api.plugins.ExtensionAware

abstract class JcefExtension : ExtensionAware {
	internal companion object {
		const val DEFAULT_TASK_GROUP = "jcef"
	}
}
