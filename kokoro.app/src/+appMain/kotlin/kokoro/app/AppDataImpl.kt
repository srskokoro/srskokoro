package kokoro.app

import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.io.SYSTEM
import kokoro.internal.io.ensureDirs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import kotlin.jvm.JvmField

@Deprecated(SPECIAL_USE_DEPRECATION)
@PublishedApi
internal object AppDataImpl {

	/** @see AppData.mainDir */
	@JvmField val mainDir: Path

	/** @see AppData.collectionsDir */
	@JvmField val collectionsDir: Path?

	@JvmField val config: MutableStateFlow<AppConfig>

	init {
		val init = @Suppress("DEPRECATION") `AppDataImpl-mainDir-init`

		val fs = FileSystem.SYSTEM
		val mainDir = fs.canonicalize(init.toPath())
		this.mainDir = mainDir

		val config = fs.openReadWrite(mainDir / "config.json").use(fun(h): AppConfig {
			if (h.size() > 0) {
				try {
					return Json.decodeFromString<AppConfig>(
						h.source().buffer().readUtf8()
					)
				} catch (ex: Throwable) {
					ex.printStackTrace()
				}
			}
			h.write(0, byteArrayOf(
				'{'.code.toByte(),
				'}'.code.toByte()
			), 0, 2)
			return AppConfig()
		})
		this.config = MutableStateFlow(config)

		this.collectionsDir = run(fun(): Path? {
			val it = config.collectionsPath?.toPath()
				?: @Suppress("DEPRECATION") `AppDataImpl-collectionsDir-default`()
			if (it != null) {
				try {
					return fs.canonicalize(
						it.ensureDirs()
					)
				} catch (ex: IOException) {
					ex.printStackTrace()
				}
			}
			return null
		})
	}
}

/**
 * WARNING: Must specify a path to an already "existing" directory.
 */
@Deprecated(SPECIAL_USE_DEPRECATION)
internal lateinit var `AppDataImpl-mainDir-init`: String
