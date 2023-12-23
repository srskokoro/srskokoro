package build.dependencies

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.io.File
import java.nio.CharBuffer

abstract class DepsCoderInvalidate : ValueSource<Boolean, DepsCoderInvalidate.Parameters> {

	interface Parameters : ValueSourceParameters {
		val depsExport: RegularFileProperty
	}

	companion object {
		fun runOn(depsExport: File, providers: ProviderFactory) {
			providers.of(DepsCoderInvalidate::class.java) {
				parameters.depsExport.set(depsExport)
			}.get()
		}
	}

	override fun obtain(): Boolean {
		val depsExport = parameters.depsExport.get().asFile
		if (depsExport.isFile) {
			val coderVersionChars = CharArray(DepsCoder.VERSION.length)
			depsExport.reader(DepsCoder.CHARSET).use { unbuffered ->
				unbuffered.read(coderVersionChars)
			}

			if (!DepsCoder.VERSION.contentEquals(CharBuffer.wrap(coderVersionChars))) {
				depsExport.delete() // Force regeneration
			}
		}
		return false
	}
}
