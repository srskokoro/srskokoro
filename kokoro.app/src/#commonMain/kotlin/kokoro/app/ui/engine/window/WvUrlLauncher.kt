package kokoro.app.ui.engine.window

@nook internal fun interface WvUrlLauncher {

	fun launchUrlExternally(url: String)

	companion object {

		internal fun shouldOverrideUrlLoading(
			sourceUrl: String?,
			destinationUrl: String?,
			onLaunchUrlExternally: WvUrlLauncher,
		): Boolean {
			// The following attempts to compare the origins of the URLs on a
			// best-effort basis. On mismatch, launch the URL externally.
			sourceUrl?.let override@{ src ->
				val src_scheme_n = src.indexOf("://")
				if (src_scheme_n < 0) {
					// Possibly empty source URL. Do not override URL loading.
					// Allows navigation for when no page has been loaded yet.
					return@override
				}

				destinationUrl?.let launch@{ dst ->
					val dst_scheme_n = dst.indexOf("://")
					if (dst_scheme_n < 0) {
						// Invalid. Do not launch anything.
						// Also prevents navigation to a blank (empty URL) page.
						return@launch
					}

					var src_origin_n = src.indexOf('/', src_scheme_n + 3)
					if (src_origin_n < 0) src_origin_n = src.length

					var dst_origin_n = dst.indexOf('/', dst_scheme_n + 3)
					if (dst_origin_n < 0) dst_origin_n = dst.length

					if (src_origin_n == dst_origin_n && src.regionMatches(0, dst, 0, dst_origin_n)) {
						// Web origin matches!
						// Do not override URL loading.
						return@override
					}
					onLaunchUrlExternally.launchUrlExternally(dst)
				}
				return true // Override URL loading
			}
			return false // Do not override URL loading
		}
	}
}
