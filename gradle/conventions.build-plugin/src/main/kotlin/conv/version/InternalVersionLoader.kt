package conv.version

import isReleasing
import org.ajoberstar.grgit.gradle.GrgitService
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.reflect.HasPublicType
import org.gradle.kotlin.dsl.typeOf
import java.io.ByteArrayInputStream
import java.util.Properties

internal abstract class InternalVersionLoader(
	grgitService: Provider<GrgitService>,
	layout: ProjectLayout,
	providers: ProviderFactory,
) : HasPublicType {

	override fun getPublicType() = typeOf<Any>()

	val version: String?
	val versionCode: Int

	init {
		val propsBytes = providers.fileContents(
			layout.projectDirectory.run {
				file("version.properties").takeIf { it.asFile.isFile }
					?: file("gradle/version.properties")
			}
		).asBytes.orNull

		if (propsBytes != null) ByteArrayInputStream(propsBytes).use { stream ->
			val props = Properties()
			props.load(stream)
			var version = props["LATEST_RELEASE"] as String?
			var versionCode = (props["LATEST_RELEASE_CODE"] as String?)?.toIntOrNull() ?: 0
			if (!providers.isReleasing) {
				version?.let {
					val headId = grgitService.get().grgit.head().abbreviatedId
					version = "$it+commit#$headId"
				}
				versionCode += NON_RELEASE_CODE_INCREMENT
			}
			this.version = version
			this.versionCode = versionCode
		} else {
			this.version = null
			this.versionCode = 0
		}
	}
}

private const val NON_RELEASE_CODE_INCREMENT =
	26 /* <3/Fe */ +
		20 /* 3rd magic number */
