package conv.version

import org.ajoberstar.grgit.gradle.GrgitService
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.io.File
import java.io.FileInputStream
import java.io.Serializable
import java.util.Properties

internal data class InternalVersion(val name: String?, val code: Int) : Serializable

internal abstract class InternalVersionLoader : @Suppress("UnstableApiUsage") ValueSource<InternalVersion, InternalVersionLoader.Parameters> {

	interface Parameters : @Suppress("UnstableApiUsage") ValueSourceParameters {
		val grgitService: Property<GrgitService>
		val rootProjectDir: DirectoryProperty
		val releasing: Property<Boolean>
	}

	override fun obtain(): InternalVersion {
		val parameters = parameters
		val rootProjectDir = parameters.rootProjectDir.get().asFile

		val propsFile = File(rootProjectDir, "version.properties").takeIf { it.isFile }
			?: File(rootProjectDir, "gradle/version.properties").takeIf { it.isFile }

		var versionName: String?
		var versionCode: Int

		if (propsFile != null) FileInputStream(propsFile).use { stream ->
			val props = Properties()
			props.load(stream)
			versionName = props["LATEST_RELEASE"] as String?
			versionCode = (props["LATEST_RELEASE_CODE"] as String?)?.toIntOrNull() ?: 0
			if (!parameters.releasing.get()) {
				versionName?.let {
					val headId = parameters.grgitService.get().grgit.head().abbreviatedId
					versionName = "$it+commit#$headId"
				}
				versionCode += NON_RELEASE_CODE_INCREMENT
			}
		} else {
			versionName = null
			versionCode = 0
		}

		return InternalVersion(versionName, versionCode)
	}
}

private const val NON_RELEASE_CODE_INCREMENT =
	26 /* <3/Fe */ +
		20 /* 3rd magic number */
