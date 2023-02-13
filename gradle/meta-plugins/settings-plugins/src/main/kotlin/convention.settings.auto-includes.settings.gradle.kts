@file:Suppress("UnstableApiUsage")

import java.util.*

pluginManagement {
	// Include our own custom plugins
	"gradle/plugins".let { dir ->
		for (parent in arrayOf("", "../", "../../")) {
			val target = "$parent$dir"
			if (!File(rootDir, "$target/settings.gradle.kts").exists()) continue

			// Special handling if we're the main build
			if (parent == "") {
				// Share the main build's `gradle.properties`
				val propSrc = File(rootDir, "gradle.properties")
				val propDst = File(rootDir, "$target/gradle.properties")

				val propSrcLastMod = propSrc.lastModified()
				val APPROX_UNTOUCHED_MILLIS_HALVED = 5_000
				val APPROX_UNTOUCHED_MILLIS = 2 * APPROX_UNTOUCHED_MILLIS_HALVED

				if (propDst.lastModified()
						.let { it <= propSrcLastMod || it > propSrcLastMod + APPROX_UNTOUCHED_MILLIS }
				) {
					val prop = Properties()
					// NOTE: `gradle.properties` files are supposed to be
					// encoded in ISO-8859-1 (also known as Latin-1).
					// - See, https://github.com/gradle/gradle/issues/13741#issuecomment-658177619
					// - See also, https://en.wikipedia.org/wiki/.properties
					propSrc.inputStream().use {
						prop.load(it)
					}

					if (propDst.exists() && !propDst.delete()) throw FileAlreadyExistsException(
						file = propDst, reason = "Failed to delete the destination file."
					)
					propDst.outputStream().use {
						prop.store(it, "Auto-generated. DO NOT MODIFY.")
					}

					if (propDst.lastModified() > propSrcLastMod + APPROX_UNTOUCHED_MILLIS) {
						// NOTE: The OS may round the last modification time to
						// the nearest supported value -- https://stackoverflow.com/a/11547476
						propDst.setLastModified(propSrcLastMod + APPROX_UNTOUCHED_MILLIS_HALVED)
					}
				}
			}

			includeBuild(target)
			break // Done!
		}
	}
}

// Include all subfolders that contain a 'build.gradle.kts' as subprojects
rootDir.let { rootDir ->
	rootDir.list()
		?.asSequence()
		?.filter { File(rootDir, "$it/build.gradle.kts").exists() }
		?.forEach { include(it) }
}
