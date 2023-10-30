package kokoro.app

import assert
import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.coroutines.RawCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import main.AppDaemon
import okio.Path
import okio.Path.Companion.toPath
import java.io.File

@Deprecated(SPECIAL_USE_DEPRECATION)
internal actual val `AppDataImpl-config-commitScope`: CoroutineScope =
	RawCoroutineScope(AppDaemon.ClientScope.coroutineContext + Dispatchers.IO)

@Deprecated(SPECIAL_USE_DEPRECATION)
internal actual fun `AppDataImpl-collectionsDir-default`(): Path? = buildString(64) {
	append(System.getProperty("user.home"))
	append(File.separatorChar)

	append("Documents")
	append(File.separatorChar)

	@Suppress("KotlinConstantConditions")
	assert({ "Must avoid potential clash (in case they end up being stored under the same parent directory)" }) {
		AppBuildDesktop.USER_COLLECTIONS_DIR_NAME != AppBuildDesktop.APP_DATA_DIR_NAME
	}
	append(AppBuildDesktop.USER_COLLECTIONS_DIR_NAME)
}.toPath()
