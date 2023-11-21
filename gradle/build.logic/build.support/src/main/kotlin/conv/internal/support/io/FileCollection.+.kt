package conv.internal.support.io

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.model.ObjectFactory
import java.io.File

fun ObjectFactory.fileCollectionVia(builtBy: Any, from: Any): ConfigurableFileCollection {
	val r = fileCollection()
	r.builtBy(builtBy)
	r.from(from)
	return r
}

inline fun FileCollection.asFileTreeVia(objects: ObjectFactory, crossinline transform: (File) -> Any): FileTree {
	return objects.fileCollectionVia(this, { this.map(transform) }).asFileTree
}
