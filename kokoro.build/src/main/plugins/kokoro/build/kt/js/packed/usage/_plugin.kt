package kokoro.build.kt.js.packed.usage

import build.api.ProjectPlugin
import build.api.dsl.*
import kokoro.build.kt.js.packed.JS_PACKED

class _plugin : ProjectPlugin({
	configurations.channelIncoming(JS_PACKED)
})
