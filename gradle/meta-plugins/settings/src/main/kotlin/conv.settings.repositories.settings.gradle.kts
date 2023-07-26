@file:Suppress("UnstableApiUsage")

pluginManagement {
	repositories {
		gradlePluginPortal()
		mavenCentral()
		google()
	}
}
plugins {
	id("org.gradle.toolchains.foojay-resolver-convention")
}

dependencyResolutionManagement {
//	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		mavenCentral()
		google()
		maven("https://jitpack.io").content {
			// KLUDGE Work around gmazzo's `buildconfig` plugin being downloaded
			//  from `jitpack` which fails.
			excludeGroup("com.github.gmazzo.buildconfig")
		}
	}
}
