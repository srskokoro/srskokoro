package build.plugins.test.io

import io.kotest.core.spec.Spec

@Suppress("NOTHING_TO_INLINE")
inline fun Spec.TestTemp() = TestTemp.from(this)

@Suppress("NOTHING_TO_INLINE")
inline fun Spec.TestTemp(subPath: String) = TestTemp.from(this, subPath)
