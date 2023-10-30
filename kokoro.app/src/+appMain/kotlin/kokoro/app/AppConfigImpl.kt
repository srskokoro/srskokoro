package kokoro.app

import kokoro.internal.SPECIAL_USE_DEPRECATION
import kotlinx.atomicfu.atomic

@Deprecated(SPECIAL_USE_DEPRECATION)
@PublishedApi
internal object AppConfigImpl {
	@Suppress("DEPRECATION")
	val holder = atomic(AppDataImpl.consumeInitConfigTmp())
}
