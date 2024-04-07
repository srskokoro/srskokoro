package kokoro.build.jcef

import build.api.ProjectPlugin
import build.api.dsl.*

class _plugin : ProjectPlugin({
	xs().create("jcef", JcefExtension::class.java)
})
