import conv.internal.setup.*
import conv.sub.android.autoNamespace
import conv.sub.android.autoNamespace_suffix__name

plugins {
	id("conv.sub")
}

ifAndroidProject {
	// NOTE: We expect that the other convention plugin we applied has already
	// forced the parent project to be evaluated and that it's non-null.
	val parent = parent ?: throw AssertionError("Shouldn't throw here")

	// NOTE: We'll use the project directory's name, instead of the project
	// name, so that we have more freedom in changing the project name into
	// something more complex, e.g., to avoid the issue described in,
	// https://github.com/gradle/gradle/issues/847#issuecomment-1205001575
	val projectDirName = projectDir.name

	// Converts from kebab case to snake case
	val autoNamespaceSuffix = projectDirName.replace('-', '_')

	val android = androidExt
	(android as ExtensionAware).extra.let { extra ->
		extra[autoNamespace_suffix__name] = autoNamespaceSuffix
	}

	android.autoNamespace(parent) { /* Do nothing */ }
}
