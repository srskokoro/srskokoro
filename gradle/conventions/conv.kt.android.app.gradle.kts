import conv.internal.setup.*

plugins {
	id("conv.base")
	id("com.android.application")
	kotlin("android")
}

kotlin {
	setUp(this)
	setUp(compilerOptions)
}

android {
	setUp(this)

	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
}

dependencies {
	// https://kotlinlang.org/docs/gradle-configure-project.html#versions-alignment-of-transitive-dependencies
	platform("org.jetbrains.kotlin:kotlin-bom").let { bom ->
		implementation(bom)
		testImplementation(bom)
	}

	setUpTestFrameworkDeps_android {
		testImplementation(it)
	}
	setUpTestCommonDeps {
		testImplementation(it)
	}
}
