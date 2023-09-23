@file:Suppress("PackageDirectoryMismatch")

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal const val XS_wv = "wv"

val KotlinSourceSet.wv get() = getExtraneousSource(XS_wv)
