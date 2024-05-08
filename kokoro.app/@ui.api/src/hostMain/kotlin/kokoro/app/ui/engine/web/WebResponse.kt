package kokoro.app.ui.engine.web

import kokoro.internal.DEBUG
import kokoro.internal.SPECIAL_USE_DEPRECATION
import kokoro.internal.io.VoidSource
import okio.Closeable
import okio.Source
import kotlin.DeprecationLevel.ERROR
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class WebResponse : Closeable {
	@Deprecated(SPECIAL_USE_DEPRECATION, level = ERROR) @PublishedApi internal var status_: Int
	@Deprecated(SPECIAL_USE_DEPRECATION, level = ERROR) @PublishedApi internal var mimeType_: String?
	@Deprecated(SPECIAL_USE_DEPRECATION, level = ERROR) @PublishedApi internal var charset_: String?
	@Deprecated(SPECIAL_USE_DEPRECATION, level = ERROR) @PublishedApi internal var headers_: MutableMap<String, String>
	@Deprecated(SPECIAL_USE_DEPRECATION, level = ERROR) @PublishedApi internal var contentLength_: Long
	@Deprecated(SPECIAL_USE_DEPRECATION, level = ERROR) @PublishedApi internal var content_: Source

	val status inline get() = @Suppress("DEPRECATION_ERROR") status_
	val mimeType inline get() = @Suppress("DEPRECATION_ERROR") mimeType_
	val charset inline get() = @Suppress("DEPRECATION_ERROR") charset_
	val headers inline get() = @Suppress("DEPRECATION_ERROR") headers_
	val contentLength inline get() = @Suppress("DEPRECATION_ERROR") contentLength_
	val content inline get() = @Suppress("DEPRECATION_ERROR") content_

	constructor(
		status: Int,
		mimeType: String?,
		charset: String?,
		headers: MutableMap<String, String>,
		contentLength: Long,
		content: Source,
	) {
		if (DEBUG) {
			// The following check ensures that `status` is consistent with the
			// expected behavior on Android. See, `android.webkit.WebResourceResponse.setStatusCodeAndReasonPhrase()`
			if (status < 100) throw IllegalArgumentException("status code can't be less than 100.")
			if (status > 599) throw IllegalArgumentException("status code can't be greater than 599.")
			if (status in 300..399) throw IllegalArgumentException("status code can't be in the [300, 399] range.")
		}
		@Suppress("DEPRECATION_ERROR")
		status_ = status
		@Suppress("DEPRECATION_ERROR")
		mimeType_ = mimeType
		@Suppress("DEPRECATION_ERROR")
		charset_ = charset
		@Suppress("DEPRECATION_ERROR")
		headers_ = headers
		@Suppress("DEPRECATION_ERROR")
		contentLength_ = contentLength
		@Suppress("DEPRECATION_ERROR")
		content_ = content
	}

	constructor(
		status: Int,
		mimeType: String?,
		charset: String?,
		contentLength: Long,
		content: Source,
	) : this(
		status = status,
		mimeType = mimeType,
		charset = charset,
		headers = mutableMapOf(),
		contentLength = contentLength,
		content,
	)

	constructor(
		mimeType: String?,
		charset: String?,
		contentLength: Long,
		content: Source,
	) : this(
		status = 200,
		mimeType = mimeType,
		charset = charset,
		headers = mutableMapOf(),
		contentLength = contentLength,
		content,
	)

	constructor(
		mimeType: String?,
		charset: String?,
	) : this(
		mimeType = mimeType,
		charset = charset,
		contentLength = 0,
		VoidSource,
	)

	constructor(
		mimeType: String?,
	) : this(
		mimeType = mimeType,
		charset = null,
		contentLength = 0,
		VoidSource,
	)

	constructor() : this(
		mimeType = null,
	)

	// --

	@Suppress("NOTHING_TO_INLINE")
	inline fun status(status: Int): WebResponse {
		@Suppress("DEPRECATION_ERROR")
		status_ = status
		return this
	}

	inline fun mimeType(mimeType: String?, charset: String?): WebResponse {
		@Suppress("DEPRECATION_ERROR")
		mimeType_ = mimeType
		@Suppress("DEPRECATION_ERROR")
		charset_ = charset
		return this
	}

	@Suppress("NOTHING_TO_INLINE")
	inline fun headers(headers: MutableMap<String, String>): WebResponse {
		@Suppress("DEPRECATION_ERROR")
		headers_ = headers
		return this
	}

	@OptIn(ExperimentalContracts::class)
	inline fun headers(block: MutableMap<String, String>.() -> Unit): WebResponse {
		contract {
			callsInPlace(block, InvocationKind.EXACTLY_ONCE)
		}
		@Suppress("DEPRECATION_ERROR")
		headers_.block()
		return this
	}

	/**
	 * @see WebResponse.unsafeReplaceContent
	 * @see WebResponse.unsafeReplaceContentLength
	 */
	inline fun content(contentLength: Long, content: Source): WebResponse {
		this.content.close() // Close previous source to avoid leak
		@Suppress("DEPRECATION_ERROR")
		contentLength_ = contentLength
		@Suppress("DEPRECATION_ERROR")
		content_ = content
		return this
	}

	/**
	 * @see WebResponse.content
	 * @see WebResponse.unsafeReplaceContentLength
	 */
	inline fun unsafeReplaceContent(contentLength: Long, content: Source): WebResponse {
		@Suppress("DEPRECATION_ERROR")
		contentLength_ = contentLength
		@Suppress("DEPRECATION_ERROR")
		content_ = content
		return this
	}

	/**
	 * @see WebResponse.content
	 * @see WebResponse.unsafeReplaceContent
	 * @see WebResponse.unsafeReplaceContentLength
	 */
	inline fun unsafeReplaceContent(content: Source): WebResponse {
		@Suppress("DEPRECATION_ERROR")
		content_ = content
		return this
	}

	/**
	 * @see WebResponse.content
	 * @see WebResponse.unsafeReplaceContent
	 * @see WebResponse.unsafeReplaceContentLength
	 */
	inline fun unsafeReplaceContentLength(contentLength: Long): WebResponse {
		@Suppress("DEPRECATION_ERROR")
		contentLength_ = contentLength
		return this
	}

	// --

	@Suppress("OVERRIDE_BY_INLINE")
	override inline fun close() {
		content.close()
	}
}
