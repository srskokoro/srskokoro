plugins {
	base
}

group = "srs.kokoro"

configurations.all {
	if (isCanBeResolved) {
		// Fail on transitive upgrade/downgrade of direct dependency versions
		failOnDirectDependencyVersionGotcha(gradle)
	}
}
