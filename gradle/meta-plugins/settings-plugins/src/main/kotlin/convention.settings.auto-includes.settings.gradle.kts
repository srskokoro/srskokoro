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
				val APPROX_UNTOUCHED_MILLIS = 10_000

				if (propDst.lastModified()
						.let { it <= propSrcLastMod || it > propSrcLastMod + APPROX_UNTOUCHED_MILLIS }
				) {
					val prop = Properties()
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
						assert(APPROX_UNTOUCHED_MILLIS > 1000)
						propDst.setLastModified(propSrcLastMod + 1000)
					}

					// Make sure that the destination's last modification time
					// is always greater than that of the source file, even if
					// the OS rounds it -- https://stackoverflow.com/a/11547476
					run {
						var t = propSrcLastMod
						while (propDst.lastModified() <= propSrcLastMod) {
							t += 1000
							propDst.setLastModified(t)
						}
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
