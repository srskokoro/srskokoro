package kokoro.app.ui.engine.web

import kokoro.app.ui.engine.web.WebRequestHandler.Companion.EMPTY
import kokoro.app.ui.engine.web.WebRequestHandler.Companion.invoke
import kokoro.app.ui.engine.web.WebUriRouting.Builder
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmField

/** @see WebUriRouting.Builder */
@OptIn(ExperimentalContracts::class)
inline fun WebUriRouting(block: Builder.() -> Unit): WebUriRouting {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
	}
	return Builder().apply(block).build()
}

/** @see WebUriRouting.Builder */
class WebUriRouting private constructor(
	@JvmField val entries: List<Entry>,
	@JvmField val nonPrefixUriCount: Int,
) : WebUriResolver {

	companion object {

		/** @see invoke */
		@JvmField val EMPTY = WebUriRouting()

		/** @see EMPTY */
		@Suppress("NOTHING_TO_INLINE")
		inline operator fun invoke() = Builder().build()
	}

	class Entry(
		uri: String,
		isUriPrefix: Boolean,
		handler: WebRequestHandler,
	) {
		val uri: String
		val isUriPrefix: Boolean
		val handler: WebRequestHandler

		init {
			if (uri.endsWith('*')) {
				this.uri = uri.substring(0, uri.length - 1)
				this.isUriPrefix = true
			} else {
				this.uri = uri
				this.isUriPrefix = isUriPrefix
			}
			this.handler = handler
		}

		override fun equals(other: Any?): Boolean {
			return this === other ||
				other is Entry &&
				uri == other.uri &&
				isUriPrefix == other.isUriPrefix &&
				handler == other.handler
		}

		override fun hashCode(): Int {
			var h = uri.hashCode()
			h = 31 * h + isUriPrefix.hashCode()
			h = 31 * h + handler.hashCode()
			return h
		}
	}

	override fun equals(other: Any?): Boolean {
		return this === other ||
			other is WebUriRouting &&
			entries == other.entries
	}

	private var hash = 0
	override fun hashCode(): Int {
		var h = hash
		if (h == 0) {
			val entries = entries
			if (entries.isNotEmpty()) {
				h = entries.hashCode()
			}
		}
		return h
	}

	class Builder {
		@JvmField val entries = ArrayList<Entry>()

		@Suppress("NOTHING_TO_INLINE")
		inline fun route(
			uri: String, isUriPrefix: Boolean,
			handler: WebRequestHandler = WebRequestHandler.EMPTY
		) = apply {
			entries.add(Entry(uri = uri, isUriPrefix = isUriPrefix, handler))
		}

		@Suppress("NOTHING_TO_INLINE")
		inline fun route(
			uri: String, handler: WebRequestHandler = WebRequestHandler.EMPTY
		) = route(uri = uri, isUriPrefix = false, handler)

		fun sort() = apply { entries.sortWith(BuildEntriesComparator) }

		fun build(): WebUriRouting {
			val entries = entries.toTypedArray()
			entries.sortWith(BuildEntriesComparator)
			return WebUriRouting(entries.asList(), run(fun(): Int {
				var n = entries.size
				while (--n >= 0 && entries[n].isUriPrefix) Unit
				return n + 1
			}))
		}
	}

	/**
	 * @see copy
	 */
	fun builder() = Builder().also { it.entries.addAll(entries) }

	/**
	 * @see builder
	 */
	@OptIn(ExperimentalContracts::class)
	inline fun copy(block: Builder.() -> Unit): WebUriRouting {
		contract {
			callsInPlace(block, InvocationKind.EXACTLY_ONCE)
		}
		return builder().apply(block).build()
	}

	private object BuildEntriesComparator : Comparator<Entry> {

		private fun E_Duplicate(entry: Entry) = IllegalStateException(
			"Entries must not have the same `uri` and `isPrefixUri` components." +
				"\n- `uri` : " + entry.uri +
				"\n- `isPrefixUri` : " + entry.isUriPrefix
		)

		override fun compare(a: Entry, b: Entry): Int {
			var cmp = a.isUriPrefix.compareTo(b.isUriPrefix)
			if (cmp == 0) {
				cmp = a.uri.compareTo(b.uri)
				if (cmp == 0) throw E_Duplicate(a)
			}
			return cmp
		}
	}

	override fun resolve(uri: WebUri): WebRequestHandler? {
		@Suppress("NAME_SHADOWING") val uri = uri.toString()
		val comparison: (Entry) -> Int = { it.uri.compareTo(uri) }

		val n = nonPrefixUriCount
		val entries = entries

		var i = entries.binarySearch(0, n, comparison)
		if (i >= 0) return entries[i].handler

		i = entries.binarySearch(n, entries.size, comparison)
		if (i < 0) i = i.inv() - 1

		if (i >= 0) {
			val x = entries[i]
			if (uri.startsWith(x.uri)) {
				return x.handler
			}
		}
		return null
	}
}
