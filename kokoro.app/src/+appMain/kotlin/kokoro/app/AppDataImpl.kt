package kokoro.app

import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.io.SYSTEM
import kokoro.internal.io.ensureDirs
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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

	private val configPathTmp: Path
	private val configPath: Path
	@JvmField val config: MutableStateFlow<AppConfig>

	init {
		val init = @Suppress("DEPRECATION") `AppDataImpl-mainDir-init`

		val fs = FileSystem.SYSTEM
		val mainDir = fs.canonicalize(init.toPath())
		this.mainDir = mainDir

		this.configPathTmp = mainDir / "config.json.tmp"
		val configPath = mainDir / "config.json"
		this.configPath = configPath
		val config = fs.openReadWrite(configPath).use(fun(h): AppConfig {
			if (h.size() > 0) {
				try {
					val s = h.source().buffer().use { it.readUtf8() }
					return Json.decodeFromString(AppConfig.serializer(), s)
				} catch (ex: Throwable) {
					ex.printStackTrace()
				}
			}
			h.write(0, byteArrayOf(
				'{'.code.toByte(),
				'}'.code.toByte(),
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

	private val scheduleConfigCommit_launched = atomic(false)

	fun scheduleConfigCommit() {
		if (scheduleConfigCommit_launched.compareAndSet(expect = false, true)) {
			// At this point, we're the only thread who can execute the
			// following piece of code.

			(@Suppress("DEPRECATION") `AppDataImpl-config-commitScope`).launch {
				// At this point, we're still the only thread who can execute
				// the following piece of code.

				val forCommit = config.value
				val s = Json.encodeToString(AppConfig.serializer(), forCommit)

				val fs = FileSystem.SYSTEM
				val t = configPathTmp
				fs.write(t) { writeUtf8(s) }
				fs.atomicMove(t, configPath)

				// NOTE: Once the following is set, we'll no longer be the only
				// one who might be executing the piece of code following this.
				scheduleConfigCommit_launched.value = false

				if (forCommit !== config.value) scheduleConfigCommit()
			}
		}
	}
}

/**
 * WARNING: Must specify a path to an already "existing" directory.
 */
@Deprecated(SPECIAL_USE_DEPRECATION)
internal lateinit var `AppDataImpl-mainDir-init`: String
