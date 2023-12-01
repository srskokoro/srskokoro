package kokoro.app.ui.engine.window

import androidx.annotation.GuardedBy
import androidx.compose.runtime.*
import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.ui.Size
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.ui.dp
import kokoro.app.compose.SimpleFrameClock
import kokoro.app.ui.ScopedWindowFrame
import kokoro.app.ui.engine.WvBinder
import kokoro.app.ui.engine.WvSetup
import kokoro.app.ui.engine.WvUnitIdMapper
import kokoro.app.ui.engine.web.WebContext
import kokoro.app.ui.engine.web.WebContextResolver
import kokoro.app.ui.engine.web.WebOrigin
import kokoro.app.ui.engine.web.WebRequest
import kokoro.app.ui.engine.web.WebResponse
import kokoro.app.ui.engine.web.WebUri
import kokoro.internal.assertUnreachable
import kokoro.internal.coroutines.RawCoroutineScope
import kokoro.internal.getSafeStackTrace
import kokoro.jcef.Jcef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okio.buffer
import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefMessageRouter
import org.cef.callback.CefCallback
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.handler.CefMessageRouterHandlerAdapter
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.handler.CefResourceHandler
import org.cef.handler.CefResourceRequestHandler
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.misc.BoolRef
import org.cef.misc.IntRef
import org.cef.misc.StringRef
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import java.awt.AWTEvent
import java.awt.Component
import java.awt.EventQueue
import java.awt.Graphics
import java.awt.GraphicsConfiguration
import java.awt.event.ComponentEvent
import java.lang.invoke.VarHandle
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// TODO! Remove `kokoro.app.ui.wv.WvWindowFrame` as it has been replaced already
abstract class WvComposeWindowFrame @JvmOverloads constructor(
	private val setup: WvSetup,
	context: CoroutineContext = EmptyCoroutineContext,
	title: String = DEFAULT_TITLE,
	gc: GraphicsConfiguration? = DEFAULT_GRAPHICS_CONFIGURATION,
) : ScopedWindowFrame(context, title, gc) {

	@Composable
	abstract fun Content()

	// --

	class JcefState internal constructor(
		val client: CefClient,
		val browser: CefBrowser,
		val component: Component,

		internal val messageRouterScope: CoroutineScope,
		internal val messageRouter: CefMessageRouter,
		internal val contexts: WebContextResolver,
	)

	@Volatile private var _jcef: JcefState? = null
	val jcef get() = _jcef

	@GuardedBy("treeLock")
	private var composition: RedwoodComposition? = null

	override fun addNotify(): Unit = synchronized(treeLock) {
		if (_jcef == null) {
			// TODO Hook some logging, so that we can detect errors, just like
			//  in the JCEF Maven example app.

			val coroutineContext = scope.coroutineContext
			val messageRouterScope = RawCoroutineScope(
				coroutineContext + Dispatchers.Default,
				SupervisorJob(coroutineContext[Job]),
			)

			val setup = setup
			val contexts = setup.contexts
			val messageRouter = CefMessageRouter.create(JcefMessageRouterHandler(contexts, messageRouterScope))

			val client = Jcef.app.createClient()
			client.addMessageRouter(messageRouter)

			client.addRequestHandler(JcefRequestHandler(contexts))
			client.addLoadHandler(JcefLoadHandler(this))

			val browser = client.createBrowser(setup.initUrl, false, false)

			val component = browser.uiComponent
			contentPane.add(component)

			_jcef = JcefState(client, browser, component, messageRouterScope, messageRouter, contexts)
		}
		super.addNotify()
		updateUiConfiguration()
	}

	override fun removeNotify(): Unit = synchronized(treeLock) {
		// With some exemptions, do tear down in an order corresponding to the
		// "reverse" of the order in which we set up things in `addNotify()`

		composition?.cancel()
		composition = null

		super.removeNotify()

		val jcef = _jcef
		if (jcef != null) {
			// Must first be disposed before everything else; otherwise, a child
			// job might interact with an already disposed component.
			jcef.messageRouterScope.coroutineContext[Job]?.cancel()

			_jcef = null

			contentPane.remove(jcef.component)
			jcef.browser.close(true)
			jcef.client.dispose()

			// Must be disposed only after `CefClient` has already been
			// disposed; otherwise, it may still be used by `CefClient`
			jcef.messageRouter.dispose()
		}
	}

	private fun setUpComposition(origin: WebOrigin?) {
		// Execute in Swing EDT to avoid potential deadlocks, given that we lock
		// on `treeLock`.
		EventQueue.invokeLater(fun() {
			val composition: RedwoodComposition

			synchronized(treeLock) {
				val jcef = _jcef
					?: // `removeNotify()` already called.
					return // Nothing to do.

				this.composition?.cancel()

				if (origin != null) {
					val context = jcef.contexts.getWebContext(origin)
					if (context != null) {
						val binder = WvBinder(
							if (context is WvUnitIdMapper) context
							else WvUnitIdMapper.NULL,
							jcef.browser,
						)

						val widgets = setup.onNewComposition(binder)
						composition = RedwoodComposition(
							scope, binder.rootChildren, _uiConfiguration,
							widgets, binder::onConcludeChanges,
						)

						this.composition = composition
						return@synchronized
					}
				}
				this.composition = null
				return // Skip code below
			}

			// NOTE: By the time we get here, `composition` might have already
			// been cancelled by `removeNotify()` called from another thread. In
			// that case, the following code (hopefully) simply becomes a NOP.
			composition.setContent { Content() }
		})
	}

	// --

	private class JcefMessageRouterHandler(
		val contexts: WebContextResolver,
		val dispatchScope: CoroutineScope,
	) : CefMessageRouterHandlerAdapter() {

		override fun onQuery(
			browser: CefBrowser?, frame: CefFrame?,
			queryId: Long, request: String?, persistent: Boolean,
			callback: CefQueryCallback?,
		): Boolean {
			run<Unit> {
				if (callback == null) return@run
				if (request == null) return@run
				if (frame == null) return@run
				// TODO! Memoize or track `origin` per `frame`
				//  - NOTE: `frame` is only valid until the enclosing method returns
				val origin = WebOrigin.fromUri(frame.url ?: "")
				val context = contexts.getWebContext(origin) ?: return@run

				dispatch(request, callback, context)
				return true
			}
			return false
		}

		private fun dispatch(request: String, callback: CefQueryCallback, context: WebContext) {
			// TODO Support for JS `AbortController` and `AbortSignal`
			//  - See, https://developer.mozilla.org/en-US/docs/Web/API/AbortSignal#implementing_an_abortable_api
			//  - See also, https://github.com/zzdjk6/simple-abortable-promise
			dispatchScope.launch {
				val response: String
				try {
					@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "NAME_SHADOWING")
					val request = request as java.lang.String

					val responseOut = StringBuilder()
					val data: String
					var what_e = request.indexOf(':'.code)
					if (what_e >= 0) {
						val target_s = what_e + 1
						var target_e = request.indexOf(':'.code, target_s)
						if (target_e >= 0) {
							data = request.substring(target_e + 1)
						} else {
							target_e = request.length
							data = ""
						}
						responseOut.append(request, target_s, target_e)
					} else {
						what_e = request.length
						data = ""
					}

					val what = Integer.parseInt(request, 0, what_e, 10)
					responseOut.append(':')

					val result = context.onJsMessage(what, data)
					responseOut.append(result)

					response = responseOut.toString()
				} catch (ex: Throwable) {
					callback.failure(-2, ex.getSafeStackTrace())
					return@launch
				}
				callback.success(response)
			}
		}
	}

	private class JcefRequestHandler(val contexts: WebContextResolver) : CefRequestHandlerAdapter() {
		override fun getResourceRequestHandler(
			browser: CefBrowser?, frame: CefFrame?, request: CefRequest?,
			isNavigation: Boolean, isDownload: Boolean, requestInitiator: String?,
			disableDefaultHandling: BoolRef?,
		): CefResourceRequestHandler? {
			if (request != null) {
				val targetOrigin = WebOrigin.fromUri(request.url)
				val context = contexts.getWebContext(targetOrigin)
				if (context != null) {
					run<Unit> {
						if (
							!requestInitiator.isNullOrEmpty() &&
							!requestInitiator.startsWith("about:") &&
							!requestInitiator.startsWith("chrome:")
						) {
							val sourceOrigin = WebOrigin.fromUri(requestInitiator)
							if (!context.shouldAllowUsageFromOrigin(sourceOrigin)) {
								return@run
							}
						} else if (frame == null || !frame.isMain) {
							return@run
						}

						val webRequest = WebRequestImpl(request)
						val webResponse = context.onWebRequest(webRequest)
						webRequest.request = null

						if (webResponse != null) {
							return JcefResourceHandler(webResponse)
						}
					}
					return JcefTeapotResourceHandler
				}
			}
			return null
		}
	}

	private class WebRequestImpl(var request: CefRequest? = null) : WebRequest {
		inline val validRequest get() = request ?: throw E_Invalid()
		override val isValid: Boolean get() = request != null

		override val method: String get() = validRequest.method
		override val url: WebUri = WebUri(validRequest.url ?: "")

		private var headers: Map<String, String>? = null
		override fun headers(): Map<String, String> {
			var headers = this.headers
			if (headers == null) {
				headers = HashMap()
				validRequest.getHeaderMap(headers)
				VarHandle.releaseFence()
				this.headers = headers
			}
			return headers
		}

		override fun header(name: String): String? =
			validRequest.getHeaderByName(name)

		private fun E_Invalid() = IllegalStateException("No longer valid")
	}

	private abstract class JcefResourceHandlerAdapter : CefResourceHandler, CefResourceRequestHandlerAdapter() {
		final override fun getResourceHandler(browser: CefBrowser?, frame: CefFrame?, request: CefRequest?): CefResourceHandler = this
	}

	private object JcefTeapotResourceHandler : JcefResourceHandlerAdapter() {

		override fun processRequest(request: CefRequest?, callback: CefCallback): Boolean {
			callback.Continue()
			return true
		}

		override fun getResponseHeaders(response: CefResponse, responseLength: IntRef, redirectUrl: StringRef?) {
			responseLength.set(0)
			response.mimeType = "text/plain" // Necessary or JCEF will deem the response as invalid
			response.status = 418 // See, https://stackoverflow.com/a/56189743
		}

		override fun readResponse(dataOut: ByteArray?, bytesToRead: Int, bytesRead: IntRef?, callback: CefCallback?) = false

		override fun cancel() = Unit
	}

	private class JcefResourceHandler(val value: WebResponse) : JcefResourceHandlerAdapter() {
		private val source = value.content.buffer()

		override fun processRequest(request: CefRequest?, callback: CefCallback): Boolean {
			callback.Continue()
			return true
		}

		override fun getResponseHeaders(response: CefResponse, responseLength: IntRef?, redirectUrl: StringRef?) {
			val v = value
			response.setHeaderMap(v.headers)

			val mimeType = v.mimeType
			if (mimeType != null) {
				response.mimeType = mimeType
				val charset = v.charset
				response.setHeaderByName(
					"content-type",
					if (charset == null) mimeType
					else "$mimeType; charset=$charset",
					/* overwrite = */ true,
				)
			}

			val contentLength = v.contentLength
			if (contentLength >= 0) {
				responseLength?.set(if (contentLength <= Int.MAX_VALUE) contentLength.toInt() else -1)
				response.setHeaderByName(
					"content-length",
					contentLength.toString(),
					/* overwrite = */ true,
				)
			}

			response.status = v.status
		}

		override fun readResponse(dataOut: ByteArray, bytesToRead: Int, bytesRead: IntRef, callback: CefCallback?): Boolean {
			val contentRead = source.read(dataOut, 0, bytesToRead)
			if (contentRead < 0) return false // Done

			bytesRead.set(contentRead)
			if (contentRead > 0) return true // Something was read

			assertUnreachable { "Will load indefinitely" }
			return true // Will load indefinitely
		}

		override fun cancel(): Unit = source.close()
	}

	private class JcefLoadHandler(val owner: WvComposeWindowFrame) : CefLoadHandlerAdapter() {
		// See also, https://magpcss.org/ceforum/viewtopic.php?t=10277
		override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
			if (frame != null && frame.isMain) {
				owner.setUpComposition(
					if (httpStatusCode != 200) null
					else WebOrigin.fromUri(frame.url ?: ""),
				)
			}
		}
	}

	// --

	private val _uiConfiguration = MutableStateFlow(newUiConfiguration())
	val uiConfiguration: StateFlow<UiConfiguration> get() = _uiConfiguration

	@Suppress("MemberVisibilityCanBePrivate")
	fun updateUiConfiguration() {
		_uiConfiguration.value = newUiConfiguration()
	}

	private fun newUiConfiguration(): UiConfiguration {
		val c = _jcef?.component ?: contentPane
		val s = Size(c.width.dp, c.height.dp)
		return UiConfiguration(
			viewportSize = s,
			darkMode = isDarkMode,
		)
	}

	open val isDarkMode get() = false

	// --

	init {
		enableEvents(AWTEvent.COMPONENT_EVENT_MASK)
	}

	override fun processComponentEvent(e: ComponentEvent) {
		if (e.id == ComponentEvent.COMPONENT_RESIZED) {
			updateUiConfiguration()
		}
		super.processComponentEvent(e)
	}

	// --

	private val _frameClock = FrameClock { repaint() }
	val frameClock: MonotonicFrameClock get() = _frameClock

	private val nanoTimeStart = System.nanoTime()

	override fun paint(g: Graphics?) {
		super.paint(g)
		_frameClock.sendFrame(System.nanoTime() - nanoTimeStart)
	}

	private class FrameClock(
		private val onFrameNanosSwing: Runnable,
	) : SimpleFrameClock() {
		override fun onNewAwaiters() {
			EventQueue.invokeLater(onFrameNanosSwing)
		}
	}

	override fun onCreateScope(context: CoroutineContext): CoroutineScope {
		return super.onCreateScope(context + (super.ref + _frameClock))
	}
}
