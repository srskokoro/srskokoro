package convention.deps.internal

import convention.deps.internal.deps_versions.jvm

@Suppress("ClassName", "MemberVisibilityCanBePrivate")
abstract class deps_jvm internal constructor() {

	val toolchainConfig get() = jvm.toolchainConfig

	val verLang get() = jvm.verLang

	val verObj get() = jvm.verObj

	val ver get() = jvm.ver

	val vendor get() = jvm.vendor

	val implementation get() = jvm.implementation
}
