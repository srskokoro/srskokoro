import build.api.getExtraneousSource

plugins {
	id("build.plugins")
}

kotlinSourceSets.main {
	getExtraneousSource("plugins").srcDir("../conventions.plugins")
}
