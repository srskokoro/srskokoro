package convention.util

import convention.internal.util.*
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetWithTests
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget

fun KotlinTarget.getSourceSet(suffix: String): KotlinSourceSet =
	getSourceSets(project.kotlinExtension).getByName("${targetName}${suffix}")

val KotlinTarget.mainSourceSet get() = getSourceSet("Main")

val KotlinTargetWithTests<*, *>.testSourceSet get() = getSourceSet("Test")

val KotlinAndroidTarget.unitTestSourceSet: KotlinSourceSet
	get() = getSourceSets(project.kotlinExtension).let {
		it.findByName("${targetName}UnitTest") ?: it.getByName("${targetName}Test")
	}
