import org.gradle.api.initialization.Settings
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

/**
 * Generates a 'gradle.properties' file for the specified project directory and
 * fills it with the same entries as those in the build's 'gradle.properties'
 * file (located in the build's root directory).
 *
 * @param projectDir the project directory path (relative to the build's root
 * directory).
 */
fun Settings.shareGradleProperties(projectDir: String) {
	val srcFile = File(rootDir, "gradle.properties")
	val dstFile = File(rootDir, "$projectDir/gradle.properties")
	val dstPath = dstFile.toPath()

	if (dstFile.isFile) {
		Files.readAttributes(dstPath, BasicFileAttributes::class.java).let { dstAttr ->
			val dstModMs = dstAttr.lastModifiedTime().toMillis()
			if (dstModMs > srcFile.lastModified() && dstModMs == dstAttr.creationTime().toMillis()) {
				return@shareGradleProperties // It's likely up-to-date
			}
		}

		if (!dstFile.delete()) throw FileAlreadyExistsException(
			file = dstFile, reason = "Failed to delete the destination file."
		)
	}

	val props = Properties()
	// NOTE: 'gradle.properties' files are supposed to be encoded in ISO-8859-1
	// (also known as Latin-1).
	// - See, https://github.com/gradle/gradle/issues/13741#issuecomment-658177619
	// - See also, https://en.wikipedia.org/wiki/.properties
	srcFile.inputStream().use {
		props.load(it)
	}
	dstFile.outputStream().use {
		props.store(it, "Auto-generated. DO NOT MODIFY.")
	}

	Files.getFileAttributeView(dstPath, BasicFileAttributeView::class.java).let { dstAttrView ->
		// Sets the creation time to be the same as the last modification time.
		val lastModifiedTime = dstAttrView.readAttributes().lastModifiedTime()
		dstAttrView.setTimes(null, null, /* createTime = */ lastModifiedTime)

		// Sets the last modification time to be the same as the creation time,
		// that is, if necessary.
		//
		// Needed since the creation time may have less granularity than the
		// last modification time, and so, the former have likely been rounded
		// to the nearest supported value, making it different from the latter.
		// This hack fixes that.
		val createTime = dstAttrView.readAttributes().creationTime()
		if (createTime != lastModifiedTime) {
			dstAttrView.setTimes(/* lastModifiedTime = */ createTime, null, null)
		}
	}

	println("Auto-generated 'gradle.properties' file: ${dstFile.absolutePath}")
}
