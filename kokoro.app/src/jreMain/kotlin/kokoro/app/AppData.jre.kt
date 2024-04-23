package kokoro.app

import okio.Path

@Suppress("UnusedReceiverParameter")
val AppData.logsDir: Path inline get() = @Suppress("DEPRECATION_ERROR") `-AppDataOnJvm`.logsDirOkio
