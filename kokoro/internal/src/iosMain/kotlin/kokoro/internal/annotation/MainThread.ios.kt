package kokoro.internal.annotation

// TODO Determine why we can't use `androidx.annotation.MainThread` here
actual typealias MainThread = androidx.annotation.UiThread
