package kokoro.internal.io

import okio.Path

expect val Path.exists: Boolean

expect val Path.isRegularFile: Boolean

expect val Path.isDirectory: Boolean

expect fun Path.ensureDirs(): Path
