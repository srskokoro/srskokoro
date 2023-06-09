import conv.internal.setup.*

plugins {
	id("conv.base")
	kotlin("js")
	id("io.kotest.multiplatform")
}

kotlin {
	js(IR)
	setUp(this)
}

dependencies {
	setUpTestFrameworkDeps_js {
		testImplementation(it)
	}
	setUpTestCommonDeps {
		testImplementation(it)
	}
}
