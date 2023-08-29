package kokoro.app.ui.wv.setup

import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class WvSetupExtension @Inject constructor(
	objects: ObjectFactory,
) {
	val sourceSets = objects.domainObjectContainer(WvSetupSourceSet::class.java)
}
