package kokoro.internal

@Suppress("NOTHING_TO_INLINE")
actual inline fun StackTraceElement(
	classLoaderName: String?,
	moduleName: String?,
	moduleVersion: String?,

	declaringClass: String,
	methodName: String,

	fileName: String?,
	lineNumber: Int,
) = java.lang.StackTraceElement(
	/* classLoaderName = */ classLoaderName,
	/* moduleName = */ moduleName,
	/* moduleVersion = */ moduleVersion,

	/* declaringClass = */ declaringClass,
	/* methodName = */ methodName,

	/* fileName = */ fileName,
	/* lineNumber = */ lineNumber,
)