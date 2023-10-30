package kokoro.app

import kokoro.internal.SPECIAL_USE_DEPRECATION
import kotlinx.coroutines.CoroutineScope
import okio.Path
import okio.Path.Companion.toOkioPath

@Deprecated(SPECIAL_USE_DEPRECATION)
internal actual val `AppDataImpl-config-commitScope`: CoroutineScope
	get() = TODO("Not yet implemented")

@Deprecated(SPECIAL_USE_DEPRECATION)
internal actual fun `AppDataImpl-collectionsDir-default`(): Path? =
	MainApplication.get().getExternalFilesDir(null)?.toOkioPath()
