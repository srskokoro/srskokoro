package convention.util

import convention.*
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetWithTests
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget

fun KotlinTarget.getSourceSet(suffix: String): KotlinSourceSet =
	project.kotlinSourceSets.getByName("${targetName}${suffix}")

val KotlinTarget.mainSourceSet get() = getSourceSet("Main")

val KotlinTargetWithTests<*, *>.testSourceSet get() = getSourceSet("Test")

val KotlinAndroidTarget.unitTestSourceSet: KotlinSourceSet
	get() = project.kotlinSourceSets.let {
		it.findByName("${targetName}UnitTest") ?: it.getByName("${targetName}Test")
	}
