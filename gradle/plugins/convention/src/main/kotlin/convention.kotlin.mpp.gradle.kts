import convention.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
	id("convention.base")
	kotlin("multiplatform")
	id("io.kotest.multiplatform")
}

withAndroid {
	setUp(this)
}

internal val localKotlin = kotlin.apply {
	setUp(this)
}

// Cache as it seems costly to obtain each time
internal val kotlinSourceSets = localKotlin.sourceSets
internal val kotlinTargets = localKotlin.targets

kotlinTargets.withType<KotlinAndroidTarget> {
	with(kotlinSourceSets) {
		findByName("${name}UnitTest") ?: get("${name}Test")
	}.dependencies {
		setUpTestFrameworkDeps_android {
			implementation(it)
		}
	}
}
kotlinTargets.withType<KotlinJvmTarget> {
	testRuns["test"].executionTask.configure {
		setUp(this)
	}
	kotlinSourceSets["${name}Test"].dependencies {
		setUpTestFrameworkDeps_jvm {
			implementation(it)
		}
	}
}
kotlinSourceSets.commonTest {
	dependencies {
		setUpTestFrameworkDeps_kmp_common {
			implementation(it)
		}
		setUpTestCommonDeps {
			implementation(it)
		}
	}
}

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

	fun ExtensionContainer.addKnownSourceSetName(name: String) {
		fun ExtensionContainer.addKnownSourceSetName2(name: String) {
			"${name}Api".let { add(it, it) }
			"${name}CompileOnly".let { add(it, it) }
			"${name}Implementation".let { add(it, it) }
			"${name}RuntimeOnly".let { add(it, it) }
		}

		addKnownSourceSetName2("${name}Main")
		addKnownSourceSetName2("${name}Test")
	}

	for (name in knownSourceSetNames)
		exts.addKnownSourceSetName(name)
}
