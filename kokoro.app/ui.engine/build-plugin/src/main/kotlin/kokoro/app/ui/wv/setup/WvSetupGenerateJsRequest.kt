package kokoro.app.ui.wv.setup

import conv.internal.support.until
import kokoro.app.ui.wv.setup.WvSetupGenerateJsTask.Companion.WV_JS
import org.gradle.api.Named
import org.gradle.api.file.RelativePath
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.setProperty
import javax.inject.Inject
import kotlin.math.max

abstract class WvSetupGenerateJsRequest @Inject constructor(
	val baseJsPathName: String,
	objects: ObjectFactory,
	providers: ProviderFactory,
) : Named {
	override fun getName(): String = baseJsPathName

	val inputPackages: SetProperty<String> = objects.setProperty()

	init {
		val baseJsPathName = baseJsPathName
		inputPackages.add(providers.provider {
			baseJsPathName.until(max(0, baseJsPathName.lastIndexOf('.')))
		})
	}

	fun inputPackage(packageName: String) {
		inputPackages.add(packageName)
	}

	fun inputPackage(packageName: Provider<String>) {
		inputPackages.add(packageName)
	}

	// --

	private var _inputPackagesAsIncludePatterns: Collection<String>? = null
	internal val inputPackagesAsIncludePatterns
		get() = _inputPackagesAsIncludePatterns ?: kotlin.run {
			inputPackages.apply { finalizeValue() }.orNull
				?.map { it.replace('.', '/') + "/*.$WV_JS" }
				?: emptyList()
		}.also { _inputPackagesAsIncludePatterns = it }

	private var _baseJsPathNameAsRelativePath: RelativePath? = null
	internal val baseJsPathNameAsRelativePath
		get() = _baseJsPathNameAsRelativePath ?: RelativePath(true, *baseJsPathName.split('.').toTypedArray())
			.also { _baseJsPathNameAsRelativePath = it }
}
