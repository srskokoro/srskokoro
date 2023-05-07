import me.friwi.jcefmaven.EnumPlatform

plugins {
	`kotlin-dsl`
	id("com.github.gmazzo.buildconfig")
}

group = "kokoro.jcef" // Used by `com.github.gmazzo.buildconfig`

buildscript {
	dependencies {
		classpath(project.extra["jcef.maven.dep"] as String)
	}
}

run {
	val jcefBuildJcefCommitProp = "jcef.build.jcef-commit"
	val jcefBuildJcefCommit = extra[jcefBuildJcefCommitProp] as String

	val jcefBuildCefVersionProp = "jcef.build.cef-version"
	val jcefBuildCefVersion = extra[jcefBuildCefVersionProp] as String

	val jcefBuildTagProp = "jcef.build.tag"
	val jcefBuildTag = (extra[jcefBuildTagProp] as String)
		.replace("{jcef-commit}", jcefBuildJcefCommit)
		.replace("{cef-version}", jcefBuildCefVersion)

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

		buildConfigField("String", "pluginGradlePropsPath", "\"\"\"${file("gradle.properties")}\"\"\"")

		buildConfigField("String", "jcefBuildJcefCommitProp", "\"$jcefBuildJcefCommitProp\"")
		buildConfigField("String", "jcefBuildCefVersionProp", "\"$jcefBuildCefVersionProp\"")

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
