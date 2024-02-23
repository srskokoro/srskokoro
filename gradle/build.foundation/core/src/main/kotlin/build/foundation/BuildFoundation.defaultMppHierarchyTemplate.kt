package build.foundation

import build.foundation.BuildFoundation.MPP
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyBuilder
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyTemplate
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.extend

/**
 * @see setUpMppHierarchy
 * @see getMppHierarchyTemplate
 * @see setMppHierarchyTemplate
 * @see extendMppHierarchyTemplate
 */
val BuildFoundation.defaultMppHierarchyTemplate
	get() = defaultMppHierarchyTemplate_

@OptIn(ExperimentalKotlinGradlePluginApi::class)
private val defaultMppHierarchyTemplate_ = KotlinHierarchyTemplate.default.extend {
	// Extend the default hierarchy with our own custom setup
	common {
		group(MPP.jvmish) {
			withAndroidTarget()
			withJvm()
		}
		group("native") {
			group(MPP.unix) {
				group("apple")
				group("linux")
				group("androidNative")
			}
		}
		group(MPP.desktop) {
			withJvm()
			group("linux")
			group("macos")
			group("mingw")
		}
		group(MPP.mobile) {
			withAndroidTarget()
			group("ios")
		}
	}
}

fun BuildFoundation.ensureMppHierarchyTemplateDefaultNodes(
	sourceSets: NamedDomainObjectContainer<KotlinSourceSet>,
	configurations: ConfigurationContainer,
) {
	ensureMppHierarchyTemplateDefaultNode(MPP.jvmish, sourceSets, configurations)

	ensureMppHierarchyTemplateDefaultNode("apple", sourceSets, configurations)
	ensureMppHierarchyTemplateDefaultNode(MPP.unix, sourceSets, configurations)
	ensureMppHierarchyTemplateDefaultNode("native", sourceSets, configurations)

	ensureMppHierarchyTemplateDefaultNode(MPP.desktop, sourceSets, configurations)
	ensureMppHierarchyTemplateDefaultNode(MPP.mobile, sourceSets, configurations)
}

@Suppress("NOTHING_TO_INLINE")
private inline fun BuildFoundation.ensureMppHierarchyTemplateDefaultNode(
	node: String,
	sourceSets: NamedDomainObjectContainer<KotlinSourceSet>,
	configurations: ConfigurationContainer,
) {
	if (sourceSets.findByName("${node}Main") == null) {
		registerMppDummyConfigurations(node, configurations)
	}
}

//--

private const val mppHierarchyTemplate__extra = "--BuildFoundation-mppHierarchyTemplate--"
internal const val mppHierarchyTemplate_applied__extension = "--BuildFoundation-mppHierarchyTemplate-applied--"

/**
 * @see setUpMppHierarchy
 * @see setMppHierarchyTemplate
 * @see extendMppHierarchyTemplate
 */
fun BuildFoundation.getMppHierarchyTemplate(project: Project) =
	project.extra.getOrNull(mppHierarchyTemplate__extra) ?: defaultMppHierarchyTemplate

/**
 * @see setUpMppHierarchy
 * @see getMppHierarchyTemplate
 * @see extendMppHierarchyTemplate
 */
fun BuildFoundation.setMppHierarchyTemplate(project: Project, template: KotlinHierarchyTemplate) {
	val xs = project.extensions
	if (xs.findByName(mppHierarchyTemplate_applied__extension) != null) {
		error("It's too late to change it as it's already applied.")
	}
	xs.extraProperties.set(mppHierarchyTemplate__extra, template)
}

/**
 * @see setUpMppHierarchy
 * @see getMppHierarchyTemplate
 * @see setMppHierarchyTemplate
 */
@ExperimentalKotlinGradlePluginApi
fun BuildFoundation.extendMppHierarchyTemplate(project: Project, describe: KotlinHierarchyBuilder.Root.() -> Unit) {
	setMppHierarchyTemplate(project, getMppHierarchyTemplate(project).extend(describe))
}
