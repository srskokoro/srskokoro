package build.api.provider.sources

import org.gradle.api.Describable
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.io.File

abstract class FileModTimeSource : ValueSource<Long, FileModTimeSource.Parameters>, Describable {

	interface Parameters : ValueSourceParameters {
		val file: RegularFileProperty
	}

	companion object {

		fun get(file: File, providers: ProviderFactory) = of(file, providers).get()

		fun of(file: File, providers: ProviderFactory): Provider<Long> {
			return providers.of(FileModTimeSource::class.java) {
				parameters.file.set(file)
			}
		}
	}

	override fun getDisplayName() = "modification time of '${parameters.file.get()}'"

	override fun obtain(): Long = parameters.file.get().asFile.lastModified()
}
