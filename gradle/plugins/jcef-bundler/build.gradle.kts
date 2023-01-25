import me.friwi.jcefmaven.EnumPlatform

plugins {
	`kotlin-dsl`
	// See, https://github.com/gmazzo/gradle-buildconfig-plugin
	id("com.github.gmazzo.buildconfig") version "3.1.0"
}

group = "srs.kokoro.jcef" // Used by `com.github.gmazzo.buildconfig`

repositories {
	mavenCentral()
}

buildscript {
	dependencies {
		classpath(project.extra["jcef.maven.dep"] as String)
	}
}

run {
	val jcefBuildTagProp = "jcef.build.tag"
	val jcefBuildTag = extra[jcefBuildTagProp] as String

	val jcefBuildPlatform = EnumPlatform.getCurrentPlatform()
	val jcefBuildPlatformId = jcefBuildPlatform.identifier

	val jcefBuildRes = (extra["jcef.build.res"] as String)
		.replace("{tag}", jcefBuildTag).replace("{platform}", jcefBuildPlatformId)

	val jcefBuildDep = (extra["jcef.build.dep"] as String)
		.replace("{tag}", jcefBuildTag).replace("{platform}", jcefBuildPlatformId)

	val jcefMavenDep = extra["jcef.maven.dep"] as String

	dependencies {
		implementation(jcefBuildDep)
		implementation(jcefMavenDep)
	}

	buildConfig {
		packageName("$group")

		useKotlinOutput {
			internalVisibility = true
			topLevelConstants = true
		}

		buildConfigField("String", "jcefBuildTagProp", "\"$jcefBuildTagProp\"")
		buildConfigField("String", "jcefBuildTag", "\"$jcefBuildTag\"")

		buildConfigField(
			jcefBuildPlatform::class.qualifiedName!!, "jcefBuildPlatform",
			"${jcefBuildPlatform::class.simpleName}.${jcefBuildPlatform.name}",
		)
		buildConfigField("String", "jcefBuildPlatformId", "\"$jcefBuildPlatformId\"")

		buildConfigField("String", "jcefBuildRes", "\"$jcefBuildRes\"")

		buildConfigField("String", "jcefBuildDep", "\"$jcefBuildDep\"")
		buildConfigField("String", "jcefMavenDep", "\"$jcefMavenDep\"")
	}
}
