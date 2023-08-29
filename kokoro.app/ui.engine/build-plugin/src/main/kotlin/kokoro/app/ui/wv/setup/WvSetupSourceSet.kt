package kokoro.app.ui.wv.setup

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.domainObjectContainer
import javax.inject.Inject

abstract class WvSetupSourceSet @Inject constructor(
	val sourceSetName: String,
	objects: ObjectFactory,
) : Named {
	override fun getName() = sourceSetName

	val generateJsRequests = objects.domainObjectContainer(WvSetupGenerateJsRequest::class)

	fun generateJs(baseJsPathName: String) {
		generateJsRequests.maybeCreate(baseJsPathName)
	}

	fun generateJs(baseJsPathName: String, config: Action<in WvSetupGenerateJsRequest>) {
		config.execute(generateJsRequests.maybeCreate(baseJsPathName))
	}
}
