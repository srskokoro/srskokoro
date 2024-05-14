package build

import build.api.dsl.*
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension

internal fun Project.setUpJvmToolchain(kotlin: KotlinTopLevelExtension) {
	val map = (deps ?: return).props.map
	kotlin.jvmToolchain { setUpFrom(map) }
}
