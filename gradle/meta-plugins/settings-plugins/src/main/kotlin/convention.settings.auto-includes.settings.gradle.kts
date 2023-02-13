@file:Suppress("UnstableApiUsage")

import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

pluginManagement {
	// Include our own custom plugins
	"gradle/plugins".let { pluginsDir ->
		for (parent in arrayOf("", "../", "../../")) {
			val target = "$parent$pluginsDir"
			if (File(rootDir, "$target/settings.gradle.kts").exists().not()) continue

			// If we're the main build, share its root 'gradle.properties'
			if (parent == "") run gradlePropsShareOp@{
				val propSrc = File(rootDir, "gradle.properties")
				val propDst = File(rootDir, "$target/gradle.properties")

				if (propDst.exists()) {
					Files.readAttributes(propDst.toPath(), BasicFileAttributes::class.java).let { propDstAttr ->
						val propDstModMs = propDstAttr.lastModifiedTime().toMillis()
						if (propDstModMs > propSrc.lastModified() && propDstModMs == propDstAttr.creationTime().toMillis()) {
							return@gradlePropsShareOp // It's likely up-to-date
						}
					}

					if (!propDst.delete()) throw FileAlreadyExistsException(
						file = propDst, reason = "Failed to delete the destination file."
					)
				}

				val prop = Properties()
				// NOTE: 'gradle.properties' files are supposed to be encoded in
				// ISO-8859-1 (also known as Latin-1).
				// - See, https://github.com/gradle/gradle/issues/13741#issuecomment-658177619
				// - See also, https://en.wikipedia.org/wiki/.properties
				propSrc.inputStream().use {
					prop.load(it)
				}
				propDst.outputStream().use {
					prop.store(it, "Auto-generated. DO NOT MODIFY.")
				}

				Files.getFileAttributeView(propDst.toPath(), BasicFileAttributeView::class.java).let { propDstAttrView ->
					// Sets the file creation time to be the same as the last
					// modification time.
					val lastModifiedTime = propDstAttrView.readAttributes().lastModifiedTime()
					propDstAttrView.setTimes(null, null, /* createTime = */ lastModifiedTime)

					// Needed since the creation time may have less granularity
					// than the last modification time, and so, the former have
					// likely been rounded to the nearest supported value,
					// making it different from the latter.
					val createTime = propDstAttrView.readAttributes().creationTime()
					if (createTime != lastModifiedTime) {
						// Sets the last modification time to be the same as the
						// file creation time.
						propDstAttrView.setTimes(/* lastModifiedTime = */ createTime, null, null)
					}
				}

				println("Auto-generated 'gradle.properties' file under '$pluginsDir'")
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
