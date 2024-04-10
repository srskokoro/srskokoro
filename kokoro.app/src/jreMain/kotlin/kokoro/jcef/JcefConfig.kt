package kokoro.jcef

import kokoro.internal.SPECIAL_USE_DEPRECATION
import org.cef.CefSettings
import org.cef.CefSettings.LogSeverity
import java.io.File

class JcefConfig {

	@JvmField val customSchemes: List<JcefCustomScheme>
	@JvmField val stateObservers: List<JcefStateObserver>

	private val cefSettings: CefSettings

	fun toCefSettings(): CefSettings = cefSettings.clone()

	@Deprecated(SPECIAL_USE_DEPRECATION, replaceWith = ReplaceWith(
		"toCefSettings()",
	), level = DeprecationLevel.ERROR)
	internal fun asCefSettings() = cefSettings

	/**
	 * @param customSchemes
	 * @param stateObservers
	 * @param cacheDir
	 * @param logFile
	 * @param logSeverity The log severity. Only messages of this severity level
	 *   or higher will be logged.
	 * @param userAgent Value that will be returned as the "`User-Agent`" HTTP
	 *   header. If empty the default "`User-Agent`" string will be used.
	 * @param userAgentProduct Value that will be inserted as the product
	 *   portion of the default "`User-Agent`" string. If empty the Chromium
	 *   product version will be used. If [userAgent] is specified this value
	 *   will be ignored.
	 * @param remoteDebuggingPort Set to a value between 1024 and 65535 to
	 *   enable remote debugging on the specified port. For example, if 8080 is
	 *   specified, the remote debugging URL will be http://localhost:8080. CEF
	 *   can be remotely debugged from any CEF or Chrome browser window.
	 */
	@Suppress("ConvertSecondaryConstructorToPrimary")
	constructor(
		customSchemes: List<JcefCustomScheme> = emptyList(),
		stateObservers: List<JcefStateObserver> = emptyList(),

		cacheDir: File,

		// NOTE: This is deliberately not nullable since, even if logging is
		// disabled, JCEF/CEF may still attempt to write something to some log
		// file. Having a file explicitly declared here would at least let us
		// control the location of that (unwanted) file.
		logFile: File,
		logSeverity: LogSeverity = LogSeverity.LOGSEVERITY_DEFAULT,

		userAgent: String? = null,
		userAgentProduct: String? = null,

		remoteDebuggingPort: Int = 0,
	) {
		this.customSchemes = customSchemes
		this.stateObservers = stateObservers

		this.cefSettings = CefSettings().also { out ->
			cacheDir.absolutePath.let {
				out.cache_path = it
				out.root_cache_path = it
			}

			out.log_file = logFile.absolutePath
			out.log_severity = logSeverity

			out.user_agent = userAgent
			out.user_agent_product = userAgentProduct

			out.remote_debugging_port = remoteDebuggingPort
		}
	}
}
