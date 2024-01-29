package build.api.dsl

import assertk.assertAll
import assertk.assertThat
import build.test.hasExactly
import io.kotest.core.spec.style.FreeSpec
import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

class test_toJavaVersion_toJvmTarget : FreeSpec({
	"Mappings are corrects" {
		val listOfJvmTarget = JvmTarget.entries.run { subList(JvmTarget.JVM_1_8.ordinal, JvmTarget.JVM_21.ordinal + 1) }
		val listOfJavaVersion = JavaVersion.entries.run { subList(JavaVersion.VERSION_1_8.ordinal, JavaVersion.VERSION_21.ordinal + 1) }

		@Suppress("RemoveExplicitTypeArguments")
		assertAll {
			assertThat(
				listOfJavaVersion
			).hasExactly<JavaVersion>(
				listOfJvmTarget.map { it.toJavaVersion() }
			)

			assertThat(
				listOfJvmTarget
			).hasExactly<JvmTarget>(
				listOfJavaVersion.map { it.toJvmTarget() }
			)
		}
	}
})
