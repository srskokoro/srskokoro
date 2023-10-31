package kokoro.app

import assert
import kokoro.internal.DEBUG
import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.io.SYSTEM
import kokoro.internal.io.ensureDirs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import kotlinx.serialization.json.okio.encodeToBufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import kotlin.jvm.JvmField

@Deprecated(SPECIAL_USE_DEPRECATION)
@PublishedApi
internal object AppDataImpl {

	/** @see AppData.mainDir */
	@JvmField val mainDir: Path

	/** @see AppData.collectionsDir */
	@JvmField val collectionsDir: Path?

	@JvmField internal val configPathTmp: Path
	@JvmField internal val configPath: Path
	@JvmField val config: MutableStateFlow<AppConfig>

	init {
		val init = @Suppress("DEPRECATION") `AppDataImpl-mainDir-init`

		val fs = FileSystem.SYSTEM
		val mainDir = fs.canonicalize(init.toPath())
		this.mainDir = mainDir

		var isEmptyConfigFile = false
		this.configPathTmp = mainDir / "config.json.tmp"
		val configPath = mainDir / "config.json"
		this.configPath = configPath
		val config: AppConfig = try {
			fs.read(configPath, fun BufferedSource.(): AppConfig {
				//return Json.decodeFromString(AppConfig.serializer(), readUtf8())
				@OptIn(ExperimentalSerializationApi::class)
				return Json.decodeFromBufferedSource(AppConfig.serializer(), this)
			})
		} catch (ex: Throwable) {
			val m = try {
				fs.metadataOrNull(configPath)
			} catch (exx: Throwable) {
				exx.addSuppressed(ex)
				throw exx
			}
			run(fun() {
				if (m != null) {
					if (!m.isRegularFile) {
						try {
							fs.delete(configPath, false)
						} catch (exx: Throwable) {
							exx.addSuppressed(ex)
							throw exx
						}
					} else {
						val size = m.size
						if (size != null && size > 0) {
							if (DEBUG) throw ex
							ex.printStackTrace()
							return // Skip code below
						}
					}
				}
				isEmptyConfigFile = true
			})
			AppConfig()
		}
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

		if (isEmptyConfigFile) ConfigCommitter.schedule()
	}

	object ConfigCommitter {
		init {
			assert {
				@OptIn(ExperimentalStdlibApi::class)
				(@Suppress("DEPRECATION") `AppDataImpl-config-commitScope`)
					.coroutineContext[CoroutineDispatcher] == Dispatchers.IO
			}
		}

		@JvmField internal val isScheduled = MutableStateFlow(false)

		fun schedule() {
			if (!isScheduled.compareAndSet(expect = false, true)) return
			// At this point, we're the only thread who can execute the
			// following piece of code.

			(@Suppress("DEPRECATION") `AppDataImpl-config-commitScope`).launch {
				// At this point, we're still the only thread who can execute
				// the following piece of code.

				val forCommit = config.value
				//val s = Json.encodeToString(AppConfig.serializer(), forCommit)

				val fs = FileSystem.SYSTEM
				val t = configPathTmp
				try {
					//fs.write(t) { writeUtf8(s) }
					fs.write(t) {
						@OptIn(ExperimentalSerializationApi::class)
						Json.encodeToBufferedSink(AppConfig.serializer(), forCommit, this)
					}
					fs.atomicMove(t, configPath)
				} catch (ex: Throwable) {
					if (DEBUG) throw ex
					ex.printStackTrace()
				}

				// NOTE: Once the following is set, we'll no longer be the only
				// one who might be executing the piece of code following this.
				isScheduled.value = false

				if (forCommit !== config.value) schedule()
			}
		}

		suspend fun awaitCommit() {
			isScheduled.first { !it }
		}

		/** @see awaitCommit */
		fun awaitCommitBlocking() {
			if (isScheduled.value) {
				val context = @Suppress("DEPRECATION") `AppDataImpl-config-commitScope`.coroutineContext
				runBlocking(context) {
					awaitCommit()
				}
			}
		}
	}
}

/**
 * WARNING: Must specify a path to an already "existing" directory.
 */
@Deprecated(SPECIAL_USE_DEPRECATION)
internal lateinit var `AppDataImpl-mainDir-init`: String
