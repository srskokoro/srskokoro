package main

import kokoro.jcef.Jcef

/**
 * Cleanup for when our last process didn't shut down cleanly.
 */
internal fun cleanUpLastProcessCrash() {
	// Some JCEF helpers might have leaked from the last process
	Jcef.killExtraneousJcefHelpers()

	// NOTE: Do additional cleanup work here.
}
