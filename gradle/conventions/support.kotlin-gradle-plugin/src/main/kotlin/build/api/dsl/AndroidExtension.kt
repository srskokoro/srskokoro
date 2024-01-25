package build.api.dsl

/**
 * @see AndroidAppExtension
 * @see AndroidLibExtension
 * @see AndroidDynamicFeatureExtension
 * @see AndroidTestExtension
 */
typealias AndroidExtension = com.android.build.api.dsl.CommonExtension<*, *, *, *, *>

/** @see AndroidExtension */
typealias AndroidAppExtension = com.android.build.api.dsl.ApplicationExtension

/** @see AndroidExtension */
typealias AndroidLibExtension = com.android.build.api.dsl.LibraryExtension


/** @see AndroidExtension */
typealias AndroidDynamicFeatureExtension = com.android.build.api.dsl.DynamicFeatureExtension

/** @see AndroidExtension */
typealias AndroidTestExtension = com.android.build.api.dsl.TestExtension
