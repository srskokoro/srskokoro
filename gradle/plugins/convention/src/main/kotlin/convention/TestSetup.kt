package convention

import org.gradle.api.tasks.testing.Test

internal fun Test.setUpTestTask() {
	useJUnitPlatform()
}
