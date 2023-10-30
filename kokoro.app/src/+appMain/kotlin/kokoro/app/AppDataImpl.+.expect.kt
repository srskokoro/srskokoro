package kokoro.app

import kokoro.internal.SPECIAL_USE_DEPRECATION
import kotlinx.coroutines.CoroutineScope
import okio.Path

@Deprecated(SPECIAL_USE_DEPRECATION)
internal expect val `AppDataImpl-config-commitScope`: CoroutineScope

/**
 * NOTE: The returned path doesn't have to exist.
 */
@Deprecated(SPECIAL_USE_DEPRECATION)
internal expect fun `AppDataImpl-collectionsDir-default`(): Path?
