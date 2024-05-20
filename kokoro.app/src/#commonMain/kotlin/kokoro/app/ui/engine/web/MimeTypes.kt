package kokoro.app.ui.engine.web

object MimeTypes {

	const val BINARY = "application/octet-stream"

	private val mapExtToMime = buildMap {
		// From, https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types
		// - See also, https://gist.github.com/AshHeskes/6038140
		put("3g2", "video/3gpp2")
		put("3gp", "video/3gpp")
		put("7z", "application/x-7z-compressed")
		put("aac", "audio/aac")
		put("abw", "application/x-abiword")
		put("apng", "image/apng")
		put("arc", "application/x-freearc")
		put("avi", "video/x-msvideo")
		put("avif", "image/avif")
		put("azw", "application/vnd.amazon.ebook")
		put("bin", BINARY)
		put("bmp", "image/bmp")
		put("bz", "application/x-bzip")
		put("bz2", "application/x-bzip2")
		put("cda", "application/x-cdf")
		put("csh", "application/x-csh")
		put("css", "text/css")
		put("csv", "text/csv")
		put("doc", "application/msword")
		put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
		put("eot", "application/vnd.ms-fontobject")
		put("epub", "application/epub+zip")
		put("gif", "image/gif")
		put("gz", "application/gzip")
		put("htm", "text/html")
		put("html", "text/html")
		put("ico", "image/vnd.microsoft.icon")
		put("ics", "text/calendar")
		put("jar", "application/java-archive")
		put("jpeg", "image/jpeg")
		put("jpg", "image/jpeg")
		put("js", "text/javascript")
		put("json", "application/json")
		put("jsonld", "application/ld+json")
		put("mid", "audio/midi")
		put("midi", "audio/midi")
		put("mjs", "text/javascript")
		put("mp3", "audio/mpeg")
		put("mp4", "video/mp4")
		put("mpeg", "video/mpeg")
		put("mpkg", "application/vnd.apple.installer+xml")
		put("odp", "application/vnd.oasis.opendocument.presentation")
		put("ods", "application/vnd.oasis.opendocument.spreadsheet")
		put("odt", "application/vnd.oasis.opendocument.text")
		put("oga", "audio/ogg")
		put("ogv", "video/ogg")
		put("ogx", "application/ogg")
		put("opus", "audio/opus")
		put("otf", "font/otf")
		put("pdf", "application/pdf")
		put("php", "application/x-httpd-php")
		put("png", "image/png")
		put("ppt", "application/vnd.ms-powerpoint")
		put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation")
		put("rar", "application/vnd.rar")
		put("rtf", "application/rtf")
		put("sh", "application/x-sh")
		put("svg", "image/svg+xml")
		put("tar", "application/x-tar")
		put("tif", "image/tiff")
		put("tiff", "image/tiff")
		put("ts", "video/mp2t")
		put("ttf", "font/ttf")
		put("txt", "text/plain")
		put("vsd", "application/vnd.visio")
		put("wav", "audio/wav")
		put("weba", "audio/webm")
		put("webm", "video/webm")
		put("webp", "image/webp")
		put("woff", "font/woff")
		put("woff2", "font/woff2")
		put("xhtml", "application/xhtml+xml")
		put("xls", "application/vnd.ms-excel")
		put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
		put("xml", "application/xml")
		put("xul", "application/vnd.mozilla.xul+xml")
		put("zip", "application/zip")
	}

	/**
	 * @param ext the extension (without the leading dot).
	 *
	 * @see BINARY
	 */
	fun queryExt(ext: String): String? = mapExtToMime[ext]

	/**
	 * @see queryExt
	 */
	fun queryExt(strWithExt: String, offset: Int): String? = mapExtToMime[strWithExt.substring(0, offset)]
}
