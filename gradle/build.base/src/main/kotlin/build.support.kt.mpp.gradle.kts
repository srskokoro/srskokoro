plugins {
	kotlin("multiplatform")
	id("build.support.kt.base")
	id("build.kotest.mpp")
}

kotlin {
	js(IR) {
		nodejs()
	}

	jvm()

	iosArm64()
	iosSimulatorArm64()
	iosX64()

	linuxX64()
	linuxArm64()
	macosX64()
	macosArm64()
	mingwX64()
}
