package kokoro.internal

@Suppress("NOTHING_TO_INLINE")
inline fun StackTraceElement(
	declaringClass: String,
	methodName: String,
	fileName: String?,
	lineNumber: Int,
) = java.lang.StackTraceElement(
	/* declaringClass = */ declaringClass,
	/* methodName = */ methodName,
	/* fileName = */ fileName,
	/* lineNumber = */ lineNumber,
)

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
