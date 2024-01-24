package build.api.dsl

/**
 * @see AndroidAppExtension
 * @see AndroidLibExtension
 */
typealias AndroidExtension = com.android.build.api.dsl.CommonExtension<*, *, *, *, *>

/** @see AndroidExtension */
typealias AndroidAppExtension = com.android.build.api.dsl.ApplicationExtension

/** @see AndroidExtension */
typealias AndroidLibExtension = com.android.build.api.dsl.LibraryExtension
