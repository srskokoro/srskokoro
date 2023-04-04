package conv.deps.spec

import conv.deps.ModuleId
import conv.deps.Version

class DependencyBundleSpec internal constructor() {
	val modules: MutableMap<ModuleId, Version?> = HashMap()
}
