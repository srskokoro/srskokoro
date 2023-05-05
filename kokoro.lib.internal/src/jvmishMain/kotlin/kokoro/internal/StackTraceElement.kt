package kokoro.internal

@Suppress("RemoveRedundantQualifierName")
expect inline fun StackTraceElement(
	classLoaderName: String?,
	moduleName: String?,
	moduleVersion: String?,

	declaringClass: String,
	methodName: String,

	fileName: String?,
	lineNumber: Int,
): java.lang.StackTraceElement
