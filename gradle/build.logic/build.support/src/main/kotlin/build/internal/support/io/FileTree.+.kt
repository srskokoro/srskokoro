package build.internal.support.io

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileTree
import org.gradle.api.internal.file.UnionFileTree
import org.gradle.api.model.ObjectFactory

fun Project.emptyFileTree(): FileTree = objects.emptyFileTree()

// See, https://discuss.gradle.org/t/re-how-to-create-an-empty-filetree/14362
fun ObjectFactory.emptyFileTree(): FileTree = fileCollection().asFileTree

/** @see org.gradle.api.Project.fileTree */
fun ObjectFactory.fileTree(baseDir: Any): ConfigurableFileTree {
	val ft = fileTree()
	ft.from(baseDir)
	return ft
}

fun FileTree.addWith(fileTree: FileTree): FileTree {
	if (this is UnionFileTree) {
		addToUnion(fileTree)
		return this
	}
	return plus(fileTree)
}
