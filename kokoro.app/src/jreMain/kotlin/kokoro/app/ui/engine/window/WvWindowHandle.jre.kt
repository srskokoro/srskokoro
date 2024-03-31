package kokoro.app.ui.engine.window

import kokoro.app.ui.engine.UiBus
import kokoro.internal.DEBUG
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

@OptIn(nook::class)
actual class WvWindowHandle @nook actual constructor(
	id: String?,
	windowFactoryId: WvWindowFactoryId,
	parent: WvWindowManager?,
) : WvWindowManager(windowFactoryId, parent) {

	/** WARNING: Must only be modified from the main thread. */
	@JvmField @nook var id_ = id

	@Suppress("OVERRIDE_BY_INLINE")
	actual override val id: String?
		inline get() = id_

	// --

	@nook internal interface Peer {
		val scope: CoroutineScope
		@MainThread fun onLaunch()
		fun dispose()
	}

	private class WindowlessPeer(
		override val scope: CoroutineScope,
	) : Peer {
		override fun onLaunch() = Unit
		override fun dispose() {
			scope.coroutineContext[Job]?.cancel(null)
		}
	}

	/** WARNING: Must only be modified from the main thread. */
	@JvmField @nook internal var peer_: Peer? = null

	init {
		if (parent is WvWindowHandle) {
			val parentPeer = parent.ensurePeer()
			init(parentPeer.scope.coroutineContext)
		}
	}

	@MainThread
	fun init(coroutineContext: CoroutineContext) {
		assertThreadMain()
		if (peer_ != null) throw E_AlreadyInit()
		peer_ = if (!windowFactoryId.isNothing) {
			WvWindowFrame(this, coroutineContext)
		} else {
			WindowlessPeer(CoroutineScope(coroutineContext))
		}
	}

	@Suppress("NOTHING_TO_INLINE")
	@MainThread
	private inline fun ensurePeer() =
		peer_ ?: throw E_NotYetInit()

	@Suppress("NOTHING_TO_INLINE")
	@MainThread
	@nook internal inline fun detachPeer() {
		peer_ = null
	}

	// --

	@MainThread
	actual override fun launch() {
		assertThreadMain()
		if (id_ == null) throw E_Closed()
		if (DEBUG) windowFactoryId.let { fid ->
			if (fid.isNothing || WvWindowFactory.get(fid) == null) error(
				"Window factory ID cannot be used to launch windows: $fid"
			)
		}
		ensurePeer().onLaunch()
	}

	@MainThread
	actual override fun <T> postOrDiscard(bus: UiBus<T>, value: T): Boolean {
		TODO("Not yet implemented")
	}

	// --

	@Suppress("OVERRIDE_BY_INLINE")
	actual override val isClosed: Boolean
		inline get() = id_ == null

	@MainThread
	actual override fun onClose() {
		val id = id_ // Backup
		id_ = null // Marks as closed now (so that we don't get called recursively)
		peer_?.let { peer ->
			try {
				peer.dispose()
			} catch (ex: Throwable) {
				id_ = id // Revert
				throw ex
			}
			detachPeer()
		}
	}

	// --

	actual companion object {
		private fun E_AlreadyInit() = IllegalStateException("`init()` already called")
		private fun E_NotYetInit() = IllegalStateException("Must first call `init()`")
	}
}
