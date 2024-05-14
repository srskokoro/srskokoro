package org.gradle.kotlin.dsl

import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * See, [Plugin Marker Artifacts | Using Plugins | Gradle User Manual](https://docs.gradle.org/8.5/userguide/plugins.html#sec:plugin_markers)
 */
@Suppress("NOTHING_TO_INLINE", "UnusedReceiverParameter")
inline fun DependencyHandler.pluginMarker(pluginId: String) = "$pluginId:$pluginId.gradle.plugin"

/** @see pluginMarker */
@Suppress("NOTHING_TO_INLINE")
inline fun DependencyHandler.plugin(pluginId: String) = pluginMarker(pluginId)
