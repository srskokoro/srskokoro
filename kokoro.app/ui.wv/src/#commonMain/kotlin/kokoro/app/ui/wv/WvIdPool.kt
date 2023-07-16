package kokoro.app.ui.wv

import assert
import korlibs.datastructure.IntDeque
import kotlin.jvm.JvmInline

@JvmInline
internal value class WvIdPool private constructor(private val pool: WvIdPoolState) {
	// TODO Optimize by using our own queueing implementation

	constructor() : this(WvIdPoolState())

	fun obtainId(): Int {
		if (pool.recycled > 0) {
			val id = pool.removeFirst() // Throws if actually empty
			pool.recycled--
			return id
		}
		// NOTE: We shouldn't throw here even if the following results in an
		// overflow: let the client of this API detect for invalid IDs (and
		// overflows) obtained from this method.
		return ++pool.lastGeneratedId
	}

	fun retireId(id: Int) {
		// NOTE: Invalid IDs shouldn't be fed to this method. However, whether
		// or not the ID supplied here is valid should be up to the client of
		// this API to decide.
		pool.addLast(id)
	}

	fun onClusterRetiredIdsForRecycling(): Boolean {
		val retired = pool.size - (pool.recycling + pool.recycled)
		if (retired > 0) {
			pool.recyclingClusters.addLast(retired)
			pool.recycling += retired
			return true
		}
		assert { retired == 0 }
		return false
	}

	fun onPromoteClusteredIdsAsRecycled() {
		val clustered = pool.recyclingClusters.removeFirst() // Throws if actually empty
		assert { clustered >= 0 }

		pool.recycling -= clustered
		assert { pool.recycling >= 0 }

		pool.recycled += clustered
	}
}

private class WvIdPoolState : IntDeque() {
	var lastGeneratedId = 0

	var recycled = 0
	var recycling = 0

	val recyclingClusters = IntDeque()
}
