package build.kt.jvm.app.packaged

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.*
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.experimental.and
import kotlin.experimental.or

abstract class PackagedSpec @Inject constructor(objects: ObjectFactory) {

	@get:Input
	val bundleName: Property<String> = objects.property()

	@get:InputFiles
	val bundleAdditions: ConfigurableFileCollection = objects.fileCollection()

	// --

	/**
	 * A unique application identifier.
	 *
	 * - May only contain alphanumeric (A-Z,a-z,0-9), hyphen (-), and period (.)
	 * characters.
	 * - Use of a reverse DNS notation (e.g. `com.mycompany.myapp`) is
	 * recommended.
	 *
	 * Used by the [`--mac-package-identifier`](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html#option-mac-package-identifier)
	 * option passed to the `jpackage` command.
	 */
	@get:Input
	val appNs: Property<String> = objects.property()

	@get:Input
	val appTitle: Property<String> = objects.property()

	/**
	 * This name should ideally be less than 16 characters long and should be
	 * suitable for display in the menu bar, in the dock (on macOS), etc.
	 *
	 * Used by the [`--mac-package-name`](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html#option-mac-package-name)
	 * option passed to the `jpackage` command.
	 */
	@get:Input
	val appTitleShort: Property<String> = objects.property()

	/**
	 * Version code used for the output package.
	 *
	 * This is more restrictive than [project.version][org.gradle.api.Project.setVersion]:
	 * the value must consist of 2 or 3 nonnegative integer components. This
	 * restriction exists so as to ensure that the version can be used for all
	 * supported platforms.
	 *
	 * Used by the [`--app-version`](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html#option-app-version)
	 * option passed to the `jpackage` command.
	 */
	@get:Optional
	@get:Input
	val packageVersionCode: Property<String> = objects.property()

	@get:Optional
	@get:Input
	val description: Property<String> = objects.property()

	@get:Optional
	@get:Input
	val vendor: Property<String> = objects.property()

	@get:Optional
	@get:Input
	val copyright: Property<String> = objects.property()

	@get:Optional
	@get:InputFile
	val licenseFile: RegularFileProperty = objects.fileProperty()

	@get:Input
	val licenseFileName: Property<String> = objects.property()

	// --

	@get:Input
	val jvmArgs: ListProperty<String> = objects.listProperty()

	@get:Internal
	val jpackageResources: DirectoryProperty = objects.directoryProperty()

	@Suppress("unused")
	// NOTE: While we could've used `@InputDirectory`, that apparently implies
	// `@IgnoreEmptyDirectories`, which we don't want. Hence this hack.
	@get:InputFiles
	internal val jpackageResourcesAsFileTree: FileTree = jpackageResources.asFileTree

	// --

	init {
		appTitle.convention(bundleName)
		appTitleShort.convention(appTitle)
		licenseFileName.convention(licenseFile
			.map { it.asFile.name }.orElse("LICENSE"))
	}

	companion object {
		private val appNs_regex = Pattern.compile("""[A-Za-z0-9\-.]+""")
		private val packageVersionCode_regex = Pattern.compile("""\d+\.\d+(?:\.\d+)?""")
	}

	fun validate(logger: Logger) {
		check(appNs_regex.matcher(appNs.get()).matches()) {
			"Property `${::appNs.name}` is invalid."
		}
		packageVersionCode.orNull?.let {
			check(packageVersionCode_regex.matcher(it).matches()) {
				"Property `${::packageVersionCode.name}` is invalid.\n" +
					"- The version code must consist of 2 or 3 nonnegative integer components (e.g., \"1.3\")\n" +
					"- Current value: $it"
			}
		}

		if (appTitleShort.get().length >= 16) logger.warn("" +
			"`Value for ${::appTitleShort.name}` seems too long to be " +
			"displayed in the menu bar, in the dock (on macOS), etc."
		)

		licenseFileName.get().let {
			if (it.equals("LICENSE", ignoreCase = true)) return@let
			if (it.startsWith("LICENSE.", ignoreCase = true)) return@let
			if (it.endsWith(".LICENSE", ignoreCase = true)) return@let

			if (it.equals("COPYING", ignoreCase = true)) return@let
			if (it.startsWith("COPYING.", ignoreCase = true)) return@let

			logger.warn(
				"""
				License file should ideally be named 'LICENSE' or 'COPYING' (or similar).
				- Current license filename: {}
				""".trimIndent(),
				it,
			)
		}
	}

	/**
	 * Used by the [`--win-upgrade-uuid`](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html#option-win-upgrade-uuid)
	 * option passed to the `jpackage` command.
	 */
	@get:Internal
	val packageUuid: Provider<UUID> = appNs.map { appNs ->
		// NOTE: The following generates a v5 UUID.
		// - See also, https://stackoverflow.com/q/10867405
		//
		// While `jpackage` should already do this for us, its current
		// implementation (as of JDK 21) generates a v3 UUID inferred from the
		// vendor and application name. However, we want a v5 UUID, generated
		// from some unique application identifier that we control.

		PackagedSpec_toPackageUuid(appNs = appNs)
	}
}

/** @see PackagedSpec.packageUuid */
internal fun PackagedSpec_toPackageUuid(appNs: String): UUID {
	// `NameSpace_URL` from RFC 4122: 6ba7b811-9dad-11d1-80b4-00c04fd430c8
	val nsUrl_msb = 0x6ba7b8119dad11d1
	val nsUrl_lsb = 0x80b400c04fd430c8u.toLong()

	// See, https://www.w3.org/TR/app-uri/
	val appSch = "app://"

	MessageDigest.getInstance("SHA-1").digest(
		ByteBuffer.allocate(
			Long.SIZE_BYTES * 2 +
				appSch.length +
				appNs.length + 1
		).apply {
			putLong(nsUrl_msb)
			putLong(nsUrl_lsb)
			StandardCharsets.ISO_8859_1.let { LATIN_1 ->
				put(appSch.toByteArray(LATIN_1))
				put(appNs.toByteArray(LATIN_1))
				put('/'.code.toByte())
			}
			if (hasRemaining()) throw AssertionError()
		}.array()
	).let { x ->
		x[6] = x[6] and 0x0f  // Clear version
		x[6] = x[6] or 0x50   // Set to version 5
		x[8] = x[8] and 0x3f  // Clear variant
		x[8] = x[8] or -0x80  // Set to IETF variant
		ByteBuffer.wrap(x)
	}.run {
		return UUID(getLong(), getLong())
	}
}
