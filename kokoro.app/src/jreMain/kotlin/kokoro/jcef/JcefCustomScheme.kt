package kokoro.jcef

/**
 * @property schemeName The custom scheme to register. This should not be one of
 * the built-in schemes (e.g., HTTP, HTTPS, FILE, FTP, ABOUT, DATA, etc.)
 *
 * @property isStandard If `true`, the scheme will be treated as a standard
 * scheme. Standard schemes are subject to URL canonicalization and parsing
 * rules as defined in [Section 3.1 of RFC 1738 “Common Internet Scheme Syntax”](https://datatracker.ietf.org/doc/html/rfc1738#section-3.1).
 *
 * In particular, the syntax for standard scheme URLs must be of the form:
 *
 * > `[scheme]://[username]:[password]@[host]:[port]/[url-path]`
 *
 * Standard scheme URLs must have a host component that is a fully qualified
 * domain name as defined in [Section 3.5 of RFC 1034](https://datatracker.ietf.org/doc/html/rfc1034#section-3.5)
 * and [Section 2.1 of RFC 1123](https://datatracker.ietf.org/doc/html/rfc1123#section-2).
 * These URLs will be canonicalized to "`scheme://host/path`" in the
 * simplest case and "`scheme://username:password@host:port/path`" in the
 * most explicit case. For example, "`scheme:host/path`" and "`scheme:///host/path`"
 * will both be canonicalized to "`scheme://host/path`". The origin of a
 * standard scheme URL is the combination of scheme, host and port (i.e.,
 * "`scheme://host:port`" in the most explicit case).
 *
 * For non-standard scheme URLs only the "`scheme:`" component is parsed and
 * canonicalized. The remainder of the URL will be passed to the handler
 * as-is. For example, "`scheme:///some%20text`" will remain the same.
 * Non-standard scheme URLs cannot be used as a target for form submission.
 *
 * @property isLocal If `true`, the scheme will be treated with the same
 * security rules as those applied to "`file`" URLs. Normal pages cannot link to
 * or access local URLs. Also, by default, local URLs can only perform `XMLHttpRequest`
 * calls to the same URL (origin + path) that originated the request.
 *
 * @property isDisplayIsolated If `true`, the scheme can only be displayed from
 * other content hosted with the same scheme. For example, pages in other
 * origins cannot create `<iframe>` or hyperlinks to URLs with the scheme. For
 * schemes that must be accessible from other schemes, set this value to `false`,
 * set [isCorsEnabled] to `true`, and use CORS "`Access-Control-Allow-Origin`"
 * headers to further restrict access.
 *
 * @property isSecure If `true`, the scheme will be treated with the same
 * security rules as those applied to "`https`" URLs. For example, loading this
 * scheme from other secure schemes will not trigger mixed content warnings.
 *
 * See also, “[Secure contexts - Security on the web | MDN](https://developer.mozilla.org/en-US/docs/Web/Security/Secure_Contexts)”
 *
 * @property isCorsEnabled If `true`, the scheme can be sent CORS requests. This
 * value should be `true` in most cases where [isStandard] is `true`.
 *
 * @property isCspBypassing If `true`, the scheme can bypass "`Content-Security-Policy`"
 * (CSP) checks. This value should be `false` in most cases where [isStandard]
 * is `true`.
 *
 * @property isFetchEnabled If `true`, the scheme can perform Fetch API
 * requests.
 */
data class JcefCustomScheme(
	val schemeName: String,
	val isStandard: Boolean = true,
	val isLocal: Boolean = false,
	val isDisplayIsolated: Boolean = true,
	val isSecure: Boolean = false,
	val isCorsEnabled: Boolean = isStandard,
	val isCspBypassing: Boolean = !isStandard,
	val isFetchEnabled: Boolean = true,
)
