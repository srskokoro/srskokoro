package kokoro.app.ui.engine.window

import kokoro.app.ui.engine.UiBus
import kokoro.app.ui.engine.UiSerialization
import kokoro.internal.annotation.AnyThread
import kokoro.internal.annotation.MainThread
import kokoro.internal.assertThreadMain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerializersModule
import kotlin.coroutines.CoroutineContext

@OptIn(nook::class)
actual class WvWindowHandle @nook internal actual constructor(
	id: WvWindowId?,
	parent: WvWindowManager?,
) : WvWindowManager(id, parent) {

	@nook interface Peer {
		val scope: CoroutineScope
		@MainThread @nook fun onLaunch()
		@MainThread @nook fun onPost(busId: String, payload: ByteArray)
		fun dispose()
	}

	private class WindowlessPeer(
		override val scope: CoroutineScope,
	) : Peer {
		@nook override fun onLaunch() = Unit
		@nook override fun onPost(busId: String, payload: ByteArray) = Unit
		override fun dispose() {
			scope.coroutineContext[Job]?.cancel(null)
		}
	}

	@JvmField @nook var peer_: Peer? = null

	init {
		if (parent is WvWindowHandle) {
			val parentPeer = parent.ensurePeer()
			init(parentPeer.scope.coroutineContext)
		}
	}

	@MainThread
	fun init(coroutineContext: CoroutineContext) {
		assertThreadMain()
		val id = id ?: throw E_Closed()
		if (peer_ != null) throw E_AlreadyInit()
		peer_ = if (!id.factoryId.isNothing) {
			WvWindowFrame(this, coroutineContext)
		} else {
			WindowlessPeer(CoroutineScope(coroutineContext))
		}
	}

	@Suppress("NOTHING_TO_INLINE")
	@MainThread
	private inline fun ensurePeer() = peer_ ?: throw E_NotYetInit()

	@Suppress("NOTHING_TO_INLINE")
	@MainThread
	@nook inline fun detachPeer() {
		peer_ = null
	}

	// --

	actual override val closeJob: Job
		get() = ensurePeer().scope.coroutineContext.job

	@MainThread
	actual override fun onClose() {
		peer_?.let { peer ->
			peer.dispose()
			detachPeer()
		}
	}

	actual companion object {

		@AnyThread
		actual fun closed(): WvWindowHandle = WvWindowHandle(null, null)

		// --

		private fun E_AlreadyInit() = IllegalStateException("`${WvWindowHandle::init.name}()` already called")
		private fun E_NotYetInit() = IllegalStateException("Must first call `${WvWindowHandle::init.name}()`")
	}

	@MainThread
	actual override fun launchOrReject(): Boolean {
		assertThreadMain()
		(id ?: return false).checkOnLaunch()
		ensurePeer().onLaunch()
		return true
	}

	@MainThread
	actual override fun <T> postOrDiscard(bus: UiBus<T>, value: T): Boolean {
		assertThreadMain()
		if (id == null) return false
		val peer = ensurePeer()
		val enc = PostSerialization.encode(value, bus.serialization) // May throw
		peer.onPost(bus.id, enc)
		return true
	}

	@nook object PostSerialization {

		inline val module: SerializersModule
			get() = UiSerialization.module

		@OptIn(ExperimentalSerializationApi::class)
		private val cbor = Cbor { serializersModule = module }

		inline fun <T> encode(
			value: T,
			serialization: SerializersModule.() -> SerializationStrategy<T>,
		): ByteArray = encode(value, serialization.invoke(module))

		fun <T> encode(value: T, serializer: SerializationStrategy<T>): ByteArray {
			@OptIn(ExperimentalSerializationApi::class)
			return cbor.encodeToByteArray(serializer, value)
		}

		inline fun <T> decode(
			bytes: ByteArray,
			deserialization: SerializersModule.() -> DeserializationStrategy<T>,
		): T = decode(bytes, deserialization.invoke(module))

		fun <T> decode(bytes: ByteArray, deserializer: DeserializationStrategy<T>): T {
			@OptIn(ExperimentalSerializationApi::class)
			return cbor.decodeFromByteArray(deserializer, bytes)
		}
	}
}
