package kokoro.app

import okio.Source

/**
 * Provides access to assets that may have been embedded as resources in the
 * library package.
 */
expect object LibAssets

expect fun LibAssets.open(path: String): Source
