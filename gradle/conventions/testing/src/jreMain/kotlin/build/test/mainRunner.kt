package build.test

import java.io.File
import kotlin.jvm.internal.CallableReference
import kotlin.jvm.internal.ClassBasedDeclarationContainer
import kotlin.jvm.internal.FunctionReferenceImpl
import kotlin.reflect.KFunction
import kotlin.reflect.KFunction0
import kotlin.reflect.KFunction1

@Suppress("NOTHING_TO_INLINE")
@JvmName("mainRunner0")
inline fun mainRunner(
	mainFunction: KFunction0<Unit>,
) = mainRunner(mainFunction, emptyList())

@Suppress("NOTHING_TO_INLINE")
@JvmName("mainRunner1")
inline fun mainRunner(
	mainFunction: KFunction1<Array<String>, Unit>,
	args: Iterable<String> = emptyList(),
) = mainRunner(mainFunction as KFunction<*>, args)

@PublishedApi
internal fun mainRunner(mainFunction: KFunction<*>, args: Iterable<String>) = run {
	require(mainFunction.name == "main") { "Must be a `main` function" }

	require(mainFunction is FunctionReferenceImpl)
	require(mainFunction.boundReceiver == CallableReference.NO_RECEIVER) {
		"Must be a top-level function"
	}

	val owner = mainFunction.owner
	require(owner is ClassBasedDeclarationContainer)

	return@run mainRunner(owner.jClass, args)
}

// --

inline fun <reified T> mainRunner(
	args: Iterable<String> = emptyList(),
) = mainRunner(T::class.java, args)

@Suppress("NOTHING_TO_INLINE")
inline fun mainRunner(
	mainClass: Class<*>,
	args: Iterable<String> = emptyList(),
) = mainRunner(mainClass.name, args)

@Suppress("NOTHING_TO_INLINE")
inline fun mainRunner(mainClass: String) =
	mainRunner(mainClass, emptyList())

fun mainRunner(mainClass: String, args: Iterable<String>): ProcessBuilder {
	val java = File(System.getProperty("java.home"), "bin/java")

	val pb = ProcessBuilder(java.path, mainClass)
	pb.environment()["CLASSPATH"] = System.getProperty("java.class.path")
	pb.command().addAll(args)

	pb.redirectErrorStream(true)
	return pb
}
