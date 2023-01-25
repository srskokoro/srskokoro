import me.friwi.jcefmaven.EnumPlatform

group = "srs.kokoro.jcef"

plugins {
	`kotlin-dsl`
	`java-gradle-plugin`
	id("com.github.gmazzo.buildconfig") version "3.1.0"
}

repositories {
	mavenCentral()
}

gradlePlugin {
	plugins {
		register("jcef-bundler") {
			id = "jcef-bundler"
			implementationClass = "$group.JcefBundlerPlugin"
		}
	}
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
		useKotlinOutput {
			internalVisibility = true
			topLevelConstants = true
		}

		packageName("$group")

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
