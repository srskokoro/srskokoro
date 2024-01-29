package build.api.dsl

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

fun JvmTarget.toJavaVersion(): JavaVersion {
	return JavaVersion.entries[JavaVersion.VERSION_1_8.ordinal + (ordinal - JvmTarget.JVM_1_8.ordinal)]
}

fun JavaVersion.toJvmTarget(): JvmTarget {
	return JvmTarget.entries[JvmTarget.JVM_1_8.ordinal + (ordinal - JavaVersion.VERSION_1_8.ordinal)]
}
