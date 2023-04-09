package kokoro.internal.io

import okio.FileSystem

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
expect val FileSystem.Companion.SYSTEM: FileSystem
