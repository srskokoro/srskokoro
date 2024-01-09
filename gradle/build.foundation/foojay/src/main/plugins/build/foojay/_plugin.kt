package build.foojay

import build.api.SettingsPlugin
import org.gradle.kotlin.dsl.*

/**
 * Applies the "Foojay Toolchains" plugin.
 *
 * See,
 * - [Toolchain Download Repositories | Toolchains for JVM projects | Gradle User Manual](https://docs.gradle.org/8.5/userguide/toolchains.html#sub:download_repositories)
 * - [gradle/foojay-toolchains: Java Toolchain Resolve Plugin based on the foojay DiscoAPI | GitHub](https://github.com/gradle/foojay-toolchains)
 */
class _plugin : SettingsPlugin({
	// NOTE: Intentionally throw for the following `extra` key if not present,
	// so as to avoid typos.
	if (java.lang.Boolean.parseBoolean(extra["build.foojay.convention"]?.toString())) {
		"org.gradle.toolchains.foojay-resolver-convention"
	} else {
		"org.gradle.toolchains.foojay-resolver"
	}.let {
		apply { plugin(it) }
	}
})
