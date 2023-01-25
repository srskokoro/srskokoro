import org.gradle.api.plugins.ExtensionContainer

plugins {
	id("convention")
}

// Make sure kotlin multiplatform plugin is applied first; throws otherwise.
plugins.getPlugin("org.jetbrains.kotlin.multiplatform")

// Adds extensions to conveniently set dependencies at the top level. See,
// - https://kotlinlang.org/docs/multiplatform-add-dependencies.html
// - https://kotlinlang.org/docs/gradle-configure-project.html#set-dependencies-at-top-level
//
// The kotlin multiplatform plugin doesn't (yet) do this for us :P
//
dependencies.extensions.let { exts ->
	// NOTE: Extensions added at configuration time doesn't (yet) generate accessors. Which is why we must do
	// this here :P -- See, https://docs.gradle.org/current/userguide/kotlin_dsl.html#kotdsl:accessor_applicability
	val knownSourceSetNames = setOf(
		"android", "desktop", "jvm"
	)
	for (name in knownSourceSetNames)
		exts.addKnownSourceSetName(name)
}

fun ExtensionContainer.addKnownSourceSetName(name: String) {
	addKnownSourceSetName2("${name}Main")
	addKnownSourceSetName2("${name}Test")
}

fun ExtensionContainer.addKnownSourceSetName2(name: String) {
	"${name}Api".let { add(it, it) }
	"${name}CompileOnly".let { add(it, it) }
	"${name}Implementation".let { add(it, it) }
	"${name}RuntimeOnly".let { add(it, it) }
}
