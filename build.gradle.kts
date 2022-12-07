// TODO: Remove once fixed, https://github.com/gradle/gradle/issues/22797
@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
	// This is necessary to avoid the plugins to be loaded multiple times in
	// each subproject's classloader
	kotlin("jvm") version libs.versions.kotlin apply false
	kotlin("multiplatform") version libs.versions.kotlin apply false
	kotlin("android") version libs.versions.kotlin apply false
	id("com.android.application") version libs.versions.android apply false
	id("com.android.library") version libs.versions.android apply false
	id("org.jetbrains.compose") version libs.versions.compose.mpp apply false
}

@Suppress("UNUSED_VARIABLE")
run {
	val javaVerInt by extra(libs.versions.java.get().toInt())

	val javaVer by extra(JavaVersion.toVersion(javaVerInt))
	val javaLangVer by extra(JavaLanguageVersion.of(javaVerInt))

	val javaVendor by extra(JvmVendorSpec.ADOPTIUM)
	val javaToolchainConfig by extra(Action<JavaToolchainSpec> {
		languageVersion.set(javaLangVer)
		vendor.set(javaVendor)
	})

	val kotlinOptJvmTarget by extra(
		if (javaVerInt <= 8) "1.$javaVerInt"
		else javaVerInt.toString()
	)
}
