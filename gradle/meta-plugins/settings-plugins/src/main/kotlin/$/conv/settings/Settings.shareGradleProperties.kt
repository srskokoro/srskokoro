@file:Suppress("PackageDirectoryMismatch")

import org.gradle.api.initialization.Settings
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

/**
 * Generates a 'gradle.properties' file for the specified project directory and
 * fills it with the same entries as those in the current build's
 * 'gradle.properties' file (located in the build's settings directory).
 *
 * @param projectDir the project directory path, relative to the build's
 * settings directory.
 *
 * @see Settings.getSettingsDir
 */
fun Settings.shareGradleProperties(projectDir: String) {
	val settingsDir = settingsDir
	val srcFile = File(settingsDir, "gradle.properties")
	val dstFile = File(settingsDir, "$projectDir/gradle.properties")
	val dstPath = dstFile.toPath()

	if (dstFile.isFile) {
		Files.readAttributes(dstPath, BasicFileAttributes::class.java).let { dstAttr ->
			val dstModMs = dstAttr.lastModifiedTime().toMillis()
			if (dstModMs > srcFile.lastModified() && dstModMs == dstAttr.creationTime().toMillis()) {
				return@shareGradleProperties // It's likely up-to-date
			}
		}
	}

	val props = Properties()

	// NOTE: 'gradle.properties' files are supposed to be encoded in ISO-8859-1
	// (also known as Latin-1).
	// - See, https://github.com/gradle/gradle/issues/13741#issuecomment-658177619
	// - See also, https://en.wikipedia.org/wiki/.properties
	srcFile.inputStream().use {
		props.load(it)
	}

	// Output to a temporary file first
	val tmp = File("${dstFile.path}.tmp")
	val tmpPath = tmp.toPath()

	// Let the following throw!
	Files.deleteIfExists(tmpPath)

	tmp.outputStream().use {
		props.store(it, "Auto-generated. DO NOT MODIFY.")
		// Necessary since file writes can be delayed by the OS (even on
		// properly closed streams) and we have to do a rename/move operation
		// later to atomically publish our changes.
		it.fd.sync()
		// ^ Same as in `androidx.core.util.AtomicFile.finishWrite()`
	}

	Files.getFileAttributeView(tmpPath, BasicFileAttributeView::class.java).let { tmpAttrView ->
		// Sets the creation time to be the same as the last modification time.
		val lastModifiedTime = tmpAttrView.readAttributes().lastModifiedTime()
		tmpAttrView.setTimes(null, null, /* createTime = */ lastModifiedTime)

		// Sets the last modification time to be the same as the creation time,
		// that is, if necessary.
		//
		// Needed since the creation time may have less granularity than the
		// last modification time, and so, the former have likely been rounded
		// to the nearest supported value, making it different from the latter.
		// This hack fixes that.
		//
		// ASSUMPTION: Usually, the creation time has a higher granularity than
		// the last modification time, so the following usually won't be needed.
		// See also, https://learn.microsoft.com/en-us/windows/win32/sysinfo/file-times
		//
		val createTime = tmpAttrView.readAttributes().creationTime()
		if (createTime != lastModifiedTime) {
			tmpAttrView.setTimes(/* lastModifiedTime = */ createTime, null, null)
		}
	}

	// Atomically publish our changes via a rename/move operation
	Files.move(tmpPath, dstPath, ATOMIC_MOVE, REPLACE_EXISTING)
	// ^ Same as in `okio.NioSystemFileSystem.atomicMove()`

	println("Auto-generated 'gradle.properties' file: ${dstFile.absolutePath}")
}
