package build.kt.jvm.app.packaged.win

import build.api.file.file
import de.undercouch.gradle.tasks.download.DownloadAction
import de.undercouch.gradle.tasks.download.DownloadSpec
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.file.Deleter
import java.util.Properties
import javax.inject.Inject

abstract class WixInstallTask : DefaultTask() {

	@get:Optional
	@get:Input
	abstract val wixBinZipUrl: Property<String>

	@get:OutputFile
	abstract val wixBinZipDestination: RegularFileProperty

	@get:OutputFile
	abstract val wixBinZipPropertiesDestination: RegularFileProperty

	@get:OutputDirectory
	abstract val wixBinDestination: DirectoryProperty

	// --

	@get:Inject
	protected abstract val del: Deleter

	@get:Inject
	protected abstract val files: FileOperations

	@Suppress("LeakingThis")
	private val downloader = DownloadAction(project, this)

	@get:Internal
	val downloadSpec: DownloadSpec get() = downloader

	companion object {
		private const val SRC_URL = "SRC_URL"
	}

	@TaskAction
	open fun install() {
		val wixBinZipDestination = wixBinZipDestination.file
		val wixBinZipPropertiesDestination = wixBinZipPropertiesDestination.file
		val wixBinDestination = wixBinDestination.file

		check(wixBinZipDestination != wixBinZipPropertiesDestination) {
			"Destination file and properties file cannot be the same."
		}

		val wixBinZipUrl = wixBinZipUrl.orNull ?: kotlin.run {
			del.run {
				delete(wixBinZipDestination)
				delete(wixBinZipPropertiesDestination)
				ensureEmptyDirectory(wixBinDestination)
			}
			return // Done. Skip code below.
		}

		kotlin.run {
			if (
				wixBinZipDestination.isFile &&
				wixBinZipPropertiesDestination.isFile &&
				wixBinZipUrl == wixBinZipPropertiesDestination.inputStream().use {
					Properties().apply { load(it) }.getProperty(SRC_URL)
				}
			) {
				logger.lifecycle("Previous download will be reused.")
				return@run // Simply reuse the previous download.
			}

			// Ensure that the above check would fail if the code after this
			// doesn't complete successfully.
			del.delete(wixBinZipDestination)

			Properties().apply {
				setProperty(SRC_URL, wixBinZipUrl)
				wixBinZipPropertiesDestination.outputStream().use {
					store(it, null)
				}
			}

			// Must be done last as we rely on the presence of the downloaded
			// file to check if the entire operation succeeded last time.
			downloader.apply {
				src(wixBinZipUrl)
				dest(wixBinZipDestination)
				tempAndMove(true)
			}.execute().get()
		}

		files.sync {
			from(files.zipTree(wixBinZipDestination))
			into(wixBinDestination)
		}
	}
}
