package kokoro.jcef

import java.io.File

object Jcef {
	val bundleDir = System.getProperty("jcef.bundle")?.let { File(it) } ?: File(
		System.getenv("APP_HOME") ?: error(ERROR_JCEF_BUNDLE_DIR_NOT_SET),
		"jcef",
	)
}

private const val ERROR_JCEF_BUNDLE_DIR_NOT_SET = """
JCEF bundle location not set.
Must either set system property "jcef.bundle" (pointing to the JCEF install
directory or bundle), or set up environment variable "APP_HOME" (where
"APP_HOME/jcef" is the JCEF install directory).
"""
