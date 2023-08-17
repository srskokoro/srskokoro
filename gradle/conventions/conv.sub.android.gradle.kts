import conv.internal.setup.*
import conv.sub.android.autoNamespaceOrNop

plugins {
	id("conv.sub")
}

ifAndroidProject {
	// NOTE: We expect that the other convention plugin we applied has already
	// forced the parent project to be evaluated and that it's non-null.
	val parent = parent ?: throw AssertionError("Shouldn't throw here")

	androidExt.autoNamespaceOrNop(project, parent)
}
