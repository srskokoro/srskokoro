import org.gradle.api.initialization.Settings
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

fun Settings.shareGradleProperties(projectPath: String) {
	val propSrc = File(rootDir, "gradle.properties")
	val propDst = File(rootDir, "$projectPath/gradle.properties")

	if (propDst.exists()) {
		Files.readAttributes(propDst.toPath(), BasicFileAttributes::class.java).let { propDstAttr ->
			val propDstModMs = propDstAttr.lastModifiedTime().toMillis()
			if (propDstModMs > propSrc.lastModified() && propDstModMs == propDstAttr.creationTime().toMillis()) {
				return@shareGradleProperties // It's likely up-to-date
			}
		}

		if (!propDst.delete()) throw FileAlreadyExistsException(
			file = propDst, reason = "Failed to delete the destination file."
		)
	}

	val prop = Properties()
	// NOTE: 'gradle.properties' files are supposed to be encoded in ISO-8859-1
	// (also known as Latin-1).
	// - See, https://github.com/gradle/gradle/issues/13741#issuecomment-658177619
	// - See also, https://en.wikipedia.org/wiki/.properties
	propSrc.inputStream().use {
		prop.load(it)
	}
	propDst.outputStream().use {
		prop.store(it, "Auto-generated. DO NOT MODIFY.")
	}

	Files.getFileAttributeView(propDst.toPath(), BasicFileAttributeView::class.java).let { propDstAttrView ->
		// Sets the creation time to be the same as the last modification time.
		val lastModifiedTime = propDstAttrView.readAttributes().lastModifiedTime()
		propDstAttrView.setTimes(null, null, /* createTime = */ lastModifiedTime)

		// Sets the last modification time to be the same as the creation time,
		// that is, if necessary.
		//
		// Needed since the creation time may have less granularity than the
		// last modification time, and so, the former have likely been rounded
		// to the nearest supported value, making it different from the latter.
		// This hack fixes that.
		val createTime = propDstAttrView.readAttributes().creationTime()
		if (createTime != lastModifiedTime) {
			propDstAttrView.setTimes(/* lastModifiedTime = */ createTime, null, null)
		}
	}

	println("Auto-generated 'gradle.properties' file: ${propDst.absolutePath}")
}
