package kokoro.internal.i18n

@Suppress("NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS")
actual typealias Locale = java.util.Locale

@Suppress("NOTHING_TO_INLINE")
actual inline fun currentLocale(): Locale = Locale.getDefault()
