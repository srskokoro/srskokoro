package kokoro.app.ui.engine.window.jcef

import kokoro.app.ui.engine.web.Bom
import kokoro.app.ui.engine.web.PlatformWebRequest
import kokoro.app.ui.engine.web.WebResource
import kokoro.app.ui.engine.web.WebResponse
import kokoro.app.ui.engine.window.nook
import kokoro.internal.coroutines.CancellationSignal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import okio.BufferedSource
import okio.ByteString
import okio.buffer
import org.cef.callback.CefCallback
import org.cef.handler.CefResourceHandler
import org.cef.misc.IntRef
import org.cef.misc.StringRef
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import java.lang.invoke.VarHandle

@nook internal class CefResourceHandlerImpl(
	private val platformRequest: PlatformWebRequest,
	private val isNavigation: Boolean,
	private val handler: WebResource,
	private val scope: CoroutineScope,
) : CefResourceHandler {
	private var responseContentExhausted: Boolean = false // Guarded by `responseContent`
	private var responseContentBom: ByteString? = null // Unguarded
	private var responseContent: BufferedSource? = null
	private var response: WebResponse? = null

	suspend fun initWebResponse() {
		val r = handler.apply(platformRequest) // NOTE: Suspending call
		response = r
		responseContent = r.content.buffer()
	}

	override fun processRequest(request: CefRequest?, callback: CefCallback): Boolean {
		@OptIn(ExperimentalCoroutinesApi::class)
		scope.launch(Dispatchers.IO, start = CoroutineStart.ATOMIC) {
			try {
				initWebResponse()
				VarHandle.releaseFence()
				// ^ NOTE: We don't trust that the call below (or its internals)
				// won't be reordered before the code above.
				callback.Continue()
				return@launch // Skip code below
			} catch (ex: Throwable) {
				callback.cancel()
				throw ex
			}
		}
		return true // Handled
	}

	override fun getResponseHeaders(out: CefResponse, contentLengthOut: IntRef, redirectUrl: StringRef?) {
		val r = this.response!!

		out.status = r.status
		out.setHeaderMap(r.headers)

		var contentLength = r.contentLength
		var contentType = r.mimeType
		if (contentType != null) {
			val charset = r.charset
			if (charset != null) run<Unit> {
				// NOTE: For the MIME types listed below, CEF currently doesn't
				// support an explicit `charset` parameter for custom responses.
				// The following mitigates this issue by automatically supplying
				// a BOM.
				//
				// See also,
				// - https://www.magpcss.org/ceforum/viewtopic.php?f=10&t=894
				// - https://github.com/cefsharp/CefSharp/issues/689
				if (isNavigation) when (contentType) {
					"application/json",
					"application/xhtml+xml",
					"application/xml",
					"text/css",
					"text/html",
					"text/javascript",
					"text/plain",
					-> Bom.forMediaCharset(charset)?.let { bom ->
						// NOTE: By default, JCEF uses "ISO-8859-1" (which is a
						// superset of "US-ASCII"). Also, "US-ASCII" is the
						// default charset for the "text" MIME type -- see, https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types#structure_of_a_mime_type
						// - See also, https://github.com/cefsharp/CefSharp/issues/689#issuecomment-67086264
						val bom_n = bom.size
						if (bom_n > 0) {
							this.responseContentBom = bom
							if (contentLength >= 0)
								contentLength += bom_n
						}
						return@run // Skip code below
					}
				}
				contentType = "$contentType; charset=$charset"
			}
			// NOTE: Even if we set a "Content-Type" header, the following
			// `CefResponse.setMimeType()` configuration will overwrite it.
			// Furthermore, CEF relies on `CefResponse.setMimeType()`, so
			// setting only the "Content-Type" header will still have no effect
			// even if we omit the `CefResponse.setMimeType()` call.
			out.mimeType = contentType
		}

		if (contentLength > 0) {
			if (contentLength <= Int.MAX_VALUE) {
				contentLengthOut.set(contentLength.toInt())
			} else {
				contentLengthOut.set(-1)
				out.setHeaderByName(
					"content-length",
					contentLength.toString(),
					/* overwrite = */ true,
				)
			}
		} else {
			// NOTE: Even if `contentLength` is zero, set this to `-1`, so as to
			// ensure `readResponse()` is still called.
			contentLengthOut.set(-1)
			if (contentLength == 0L) {
				out.setHeaderByName(
					"content-length", "0",
					/* overwrite = */ true,
				)
			}
		}
	}

	override fun readResponse(dataOut: ByteArray, bytesToRead: Int, bytesRead: IntRef, callback: CefCallback): Boolean {
		val source = responseContent!!

		synchronized(source) {
			kokoro.internal.assert({ source.buffer.isOpen }) // NOTE: `source.buffer` never really closes.
			val transferred = source.buffer.read(dataOut, 0, bytesToRead)
			if (transferred > 0) {
				bytesRead.set(transferred)
				return true // Not done yet
			} else if (!responseContentExhausted) {
				bytesRead.set(0)
				// Skip below
			} else {
				source.close()
				return false // Done
			}
		}

		val bom = responseContentBom
		if (bom != null) responseContentBom = null

		@OptIn(ExperimentalCoroutinesApi::class)
		scope.launch(Dispatchers.IO, start = CoroutineStart.ATOMIC) {
			try {
				runInterruptible {
					synchronized(source) {
						if (!source.isOpen) {
							responseContentExhausted = true
							throw CancellationSignal()
						}

						if (bom != null) {
							val bom_n = bom.size
							val b = source.buffer
							b.write(bom, 0, bom_n) // Prepend BOM
							if (
								source.request(bom_n * 2L) &&
								b.rangeEquals(bom_n.toLong(), bom, 0, bom_n)
							) {
								// BOM was already present
								b.skip(bom_n.toLong())
							}
						}

						if (!source.request(bytesToRead.toLong())) {
							// Already exhausted
							responseContentExhausted = true
						}
					}
				}
				callback.Continue()
				return@launch // Skip code below
			} catch (ex: Throwable) {
				callback.cancel()
				throw ex
			}
		}
		return true // Not done yet
	}

	override fun cancel() {
		responseContent?.let {
			synchronized(it) {
				it.close()
			}
		}
	}
}
