package kokoro.app

import kokoro.internal.SPECIAL_USE_DEPRECATION
import okio.Path

/**
 * NOTE: The returned path doesn't have to exist.
 */
@Deprecated(SPECIAL_USE_DEPRECATION)
internal expect fun `AppDataImpl-collectionsDir-default`(): Path?
