package kokoro.app

import kokoro.internal.SPECIAL_USE_DEPRECATION
import kotlinx.coroutines.CoroutineScope
import okio.Path

@Deprecated(SPECIAL_USE_DEPRECATION)
internal actual val `AppDataImpl-config-commitScope`: CoroutineScope
	get() = TODO("Not yet implemented")

@Deprecated(SPECIAL_USE_DEPRECATION)
internal actual fun `AppDataImpl-collectionsDir-default`(): Path? {
	TODO("Not yet implemented")
}
