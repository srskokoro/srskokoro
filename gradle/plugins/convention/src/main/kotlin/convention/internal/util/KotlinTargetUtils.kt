package convention.internal.util

import convention.internal.setup.*
import org.gradle.api.DomainObjectCollection
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetWithTests
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget

internal fun KotlinTarget.getSourceSet(suffix: String): KotlinSourceSet =
	project.kotlinSourceSets.getByName("${targetName}${suffix}")

// --

internal val KotlinTarget.mainSourceSet get() = getSourceSet("Main")

internal inline fun KotlinTarget.mainSourceSet(
	configure: KotlinSourceSet.() -> Unit
) = configure(mainSourceSet)

internal inline fun DomainObjectCollection<out KotlinTarget>.mainSourceSets(
	crossinline configure: KotlinSourceSet.() -> Unit
) = all { configure(mainSourceSet) }

// --

internal val KotlinTargetWithTests<*, *>.testSourceSet get() = getSourceSet("Test")

internal inline fun KotlinTargetWithTests<*, *>.testSourceSet(
	configure: KotlinSourceSet.() -> Unit
) = configure(testSourceSet)

internal inline fun DomainObjectCollection<out KotlinTargetWithTests<*, *>>.testSourceSets(
	crossinline configure: KotlinSourceSet.() -> Unit
) = all { configure(testSourceSet) }

// --

internal val KotlinAndroidTarget.unitTestSourceSet: KotlinSourceSet
	get() = project.kotlinSourceSets.let {
		it.findByName("${targetName}UnitTest") ?: it.getByName("${targetName}Test")
	}

internal inline fun KotlinAndroidTarget.unitTestSourceSet(
	configure: KotlinSourceSet.() -> Unit
) = configure(unitTestSourceSet)

internal inline fun DomainObjectCollection<out KotlinAndroidTarget>.unitTestSourceSets(
	crossinline configure: KotlinSourceSet.() -> Unit
) = all { configure(unitTestSourceSet) }
