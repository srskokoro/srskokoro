@file:Suppress("PackageDirectoryMismatch")

import kokoro.app.ui.wv.setup.WvSetupGenerateJsRequest
import kokoro.app.ui.wv.setup.WvSetupSourceSet
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectProvider

fun NamedDomainObjectProvider<WvSetupSourceSet>.generateJs(baseJsPathName: String) {
	configure { generateJs(baseJsPathName = baseJsPathName) }
}

fun NamedDomainObjectProvider<WvSetupSourceSet>.generateJs(baseJsPathName: String, config: Action<in WvSetupGenerateJsRequest>) {
	configure { generateJs(baseJsPathName = baseJsPathName, config = config) }
}
