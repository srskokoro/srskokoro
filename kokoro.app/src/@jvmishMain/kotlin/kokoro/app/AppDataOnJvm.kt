package kokoro.app

import java.io.File

object AppDataOnJvm

/** @see AppData.mainDir */
expect val AppDataOnJvm.mainDir: File

/** @see AppData.cacheDir */
expect val AppDataOnJvm.cacheDir: File
