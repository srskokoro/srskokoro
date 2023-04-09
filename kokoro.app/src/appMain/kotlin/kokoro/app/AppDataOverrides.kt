package kokoro.app

import kotlin.jvm.JvmField

object AppDataOverrides {
	@JvmField var defaultRoot: String? = null

	@JvmField var roamingRoot: String? = null
	@JvmField var localRoot: String? = null
	@JvmField var cacheRoot: String? = null
}
