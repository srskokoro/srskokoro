package build.api.dsl

import org.gradle.api.tasks.testing.AbstractTestTask
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// Named like this to discourage direct access
internal const val env__extension = "--_env_--"

fun ExtensionsDelegate<out AbstractTestTask>.env(): MutableMap<String, String> =
	xs().getOrAdd(env__extension) { LinkedHashMap() }

@OptIn(ExperimentalContracts::class)
inline fun ExtensionsDelegate<out AbstractTestTask>.env(block: MutableMap<String, String>.() -> Unit) {
	contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
	return block.invoke(env())
}
