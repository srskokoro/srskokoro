package kokoro.app

import kokoro.internal.SPECIAL_USE_DEPRECATION
import okio.Path
import okio.Path.Companion.toOkioPath

@Deprecated(SPECIAL_USE_DEPRECATION)
internal actual fun `AppDataImpl-collectionsDir-default`(): Path? =
	MainApplication.get().getExternalFilesDir(null)?.toOkioPath()
