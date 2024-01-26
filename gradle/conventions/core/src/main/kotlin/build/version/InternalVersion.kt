package build.version

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.Serializable
import java.nio.charset.Charset
import java.util.Properties
import javax.inject.Inject

internal data class InternalVersion(val name: String?, val code: Int) : Serializable

internal abstract class InternalVersionLoader : ValueSource<InternalVersion, InternalVersionLoader.Parameters> {

	interface Parameters : ValueSourceParameters {
		val rootSettingsDir: DirectoryProperty
		val releasing: Property<Boolean>
	}

	@get:Inject internal abstract val execOps: ExecOperations

	override fun obtain(): InternalVersion {
		val parameters = parameters
		val rootSettingsDir = parameters.rootSettingsDir.get().asFile

		val propsFile = File(rootSettingsDir, "version.properties").takeIf { it.isFile }
			?: File(rootSettingsDir, "gradle/version.properties").takeIf { it.isFile }

		var versionName: String?
		var versionCode: Int

		if (propsFile != null) FileInputStream(propsFile).use { stream ->
			val props = Properties()
			props.load(stream)
			versionName = props["LATEST_RELEASE"] as String?
			versionCode = (props["LATEST_RELEASE_CODE"] as String?)?.toIntOrNull() ?: 0
			if (!parameters.releasing.get()) {
				versionName?.let {
					val output = ByteArrayOutputStream()
					execOps.exec {
						commandLine("git", "rev-parse", "--short", "HEAD")
						workingDir(rootSettingsDir)
						standardOutput = output
					}
					val headId = String(
						output.toByteArray(),
						Charset.defaultCharset(),
					).trim()
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

private const val NON_RELEASE_CODE_INCREMENT = 0 +
	26 /* <3/Fe */ +
	20 /* 3rd magic number */
