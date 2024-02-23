package kokoro.internal.annotation

@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(
	AnnotationTarget.FUNCTION,
	AnnotationTarget.PROPERTY_GETTER,
	AnnotationTarget.PROPERTY_SETTER,
	AnnotationTarget.CONSTRUCTOR,
	AnnotationTarget.ANNOTATION_CLASS,
	AnnotationTarget.CLASS,
	AnnotationTarget.VALUE_PARAMETER,
)
actual annotation class MainThread
