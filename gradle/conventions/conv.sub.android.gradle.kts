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

	// Converts invalid identifier characters into underscores
	val autoNamespaceSuffix = name.let { name ->
		if (name.isEmpty()) "_"
		else name.replace(Regex("\\W"), "_").let {
			if (Character.isJavaIdentifierStart(it[0])) it
			else "_$it"
		}
	}

	val android = androidExt
	(android as ExtensionAware).extra.let { extra ->
		extra[autoNamespace_suffix__name] = autoNamespaceSuffix
	}

	android.autoNamespace(parent) { /* Do nothing */ }
}
