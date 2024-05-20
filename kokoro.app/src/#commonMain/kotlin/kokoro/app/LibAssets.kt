package kokoro.app

import okio.FileNotFoundException
import okio.Source

/**
 * Provides access to assets that may have been embedded as resources in the
 * library package.
 */
expect object LibAssets

@Throws(FileNotFoundException::class)
expect fun LibAssets.open(path: String): Source
