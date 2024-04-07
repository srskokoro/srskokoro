package kokoro.build.jcef.bundler

import build.api.ProjectPlugin
import build.api.dsl.*

class _plugin : ProjectPlugin({
	xs().create("jcef", JcefBundlerExtension::class.java)
})
