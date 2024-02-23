package build.foundation

import build.api.dsl.accessors.kotlinSourceSets
import org.gradle.api.Project

@OptIn(InternalApi::class)
fun BuildFoundation.ensureMppHierarchyTemplateDefaultNodes(project: Project) {
	ensureMppHierarchyTemplateDefaultNodes(project.kotlinSourceSets, project.configurations)
}
