package convention.util

import convention.*
import org.gradle.api.DomainObjectCollection
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetWithTests
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget

fun KotlinTarget.getSourceSet(suffix: String): KotlinSourceSet =
	project.kotlinSourceSets.getByName("${targetName}${suffix}")

// --

val KotlinTarget.mainSourceSet get() = getSourceSet("Main")

inline fun KotlinTarget.mainSourceSet(
	configure: KotlinSourceSet.() -> Unit
) = configure(mainSourceSet)

inline fun DomainObjectCollection<out KotlinTarget>.mainSourceSet(
	crossinline configure: KotlinSourceSet.() -> Unit
) = all { configure(mainSourceSet) }

// --

val KotlinTargetWithTests<*, *>.testSourceSet get() = getSourceSet("Test")

inline fun KotlinTargetWithTests<*, *>.testSourceSet(
	configure: KotlinSourceSet.() -> Unit
) = configure(testSourceSet)

inline fun DomainObjectCollection<out KotlinTargetWithTests<*, *>>.testSourceSet(
	crossinline configure: KotlinSourceSet.() -> Unit
) = all { configure(testSourceSet) }

// --

val KotlinAndroidTarget.unitTestSourceSet: KotlinSourceSet
	get() = project.kotlinSourceSets.let {
		it.findByName("${targetName}UnitTest") ?: it.getByName("${targetName}Test")
	}

inline fun KotlinAndroidTarget.unitTestSourceSet(
	configure: KotlinSourceSet.() -> Unit
) = configure(unitTestSourceSet)

inline fun DomainObjectCollection<out KotlinAndroidTarget>.unitTestSourceSet(
	crossinline configure: KotlinSourceSet.() -> Unit
) = all { configure(unitTestSourceSet) }
